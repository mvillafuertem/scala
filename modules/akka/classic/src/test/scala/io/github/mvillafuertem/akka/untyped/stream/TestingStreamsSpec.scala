package io.github.mvillafuertem.akka.untyped.stream

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.Attributes.LogLevels
import akka.stream._
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, Keep, Merge, Partition, Sink, Source, ZipWith }
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.{ TestSink, TestSource }
import akka.testkit.{ TestKit, TestProbe }
import akka.util.Timeout
import akka.{ Done, NotUsed }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

/**
 * @author
 *   Miguel Villafuerte
 */
final class TestingStreamsSpec extends TestKit(ActorSystem("TestingStreams")) with AnyWordSpecLike with BeforeAndAfterAll {

  implicit val materializer: Materializer = Materializer(system)

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A simple stream" should {

    "satisfy basic assertions" in {
      // describe our test

      val simpleSource = Source(1 to 10)
      val simpleSink   = Sink.fold(0)((a: Int, b: Int) => a + b)

      val sumFuture = simpleSource.toMat(simpleSink)(Keep.right).run()
      val sum       = Await.result(sumFuture, 2 seconds)

      assert(sum == 55)
    }

    "integrate with test actors via materialized values" in {

      import akka.pattern.pipe
      import system.dispatcher

      val simpleSource = Source(1 to 10)
      val simpleSink   = Sink.fold(0)((a: Int, b: Int) => a + b)

      val probe = TestProbe()

      simpleSource.toMat(simpleSink)(Keep.right).run().pipeTo(probe.ref)

      probe.expectMsg(55)
    }

    "integrate with test-actor-based sink" in {

      val simpleSource    = Source(1 to 5)
      // 0, 1, 3, 6, 10, 15
      val flow            = Flow[Int].scan[Int](0)(_ + _)
      val streamUnderTest = simpleSource.via(flow)

      val probe     = TestProbe()
      val probeSink = Sink.actorRef(probe.ref, "completionMessage", _.printStackTrace())

      streamUnderTest.to(probeSink).run()

      probe.expectMsgAllOf(0, 1, 3, 6, 10, 15)
    }

    "integrate with streams testkit sink" in {

      val sourceUnderTest = Source(1 to 5).map(_ * 2)

      val testSink              = TestSink.probe[Int]
      val materializedTestValue = sourceUnderTest.runWith(testSink)

      materializedTestValue
        .request(5)
        .expectNext(2, 4, 6, 8, 10)
        .expectComplete()
    }

    "integrate with streams testkit source" in {
      import system.dispatcher

      val sinkUnderTest = Sink.foreach[Int] {
        case 13 => throw new RuntimeException("bad luck")
        case _  =>
      }

      val testSource                    = TestSource.probe[Int]
      val materializedTestValue         = testSource.toMat(sinkUnderTest)(Keep.both).run()
      val (testPublisher, resultFuture) = materializedTestValue

      testPublisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      resultFuture.onComplete {
        case Success(_) => fail("the sink under test should have thrown an exception on 13")
        case Failure(_) =>
      }

    }

    "test flows with a test source AND a test sink" in {
      val flowUnderTest = Flow[Int].map(_ * 2)

      val testSource = TestSource.probe[Int]
      val testSink   = TestSink.probe[Int]

      val materializedTestValue = testSource.via(flowUnderTest).toMat(testSink)(Keep.both).run()

      val (publisher, subscriber) = materializedTestValue

      publisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      subscriber
        .request(3) // Don't forget this
        .expectNext(2, 10, 26)
        .expectComplete()

    }

    "test partition flows with a test source AND a test sink" in {

      def spin(value: Int): Int = {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 10) {}
        value
      }

      val flow = Flow
        .fromGraph(GraphDSL.create() { implicit b =>
          import GraphDSL.Implicits._

          val workerCount = 4

          val partition = b.add(Partition[Int](workerCount, _ % workerCount))
          val merge     = b.add(Merge[Int](workerCount))

          for (_ <- 1 to workerCount)
            partition ~> Flow[Int].log("partition").map(spin).async ~> merge

          FlowShape(partition.in, merge.out)
        })
        .withAttributes(Attributes.logLevels(onElement = LogLevels.Info))

      val testSource = TestSource.probe[Int]
      val testSink   = TestSink.probe[Int]

      val materializedTestValue   = testSource.via(flow).toMat(testSink)(Keep.both).run()
      val (publisher, subscriber) = materializedTestValue

      publisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      subscriber
        .request(3) // Don't forget this
        .expectNext(1, 5, 13)
        .expectComplete()

    }

    "test partition flows with a test source AND a test sink with validation graph" in {

      def spin(value: Option[Int]): Int = {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 10) {}
        value.get
      }

      val validationGraph =
        GraphDSL.create() { implicit builder =>
          import GraphDSL.Implicits._

          val validationShape: FlowShape[Int, Option[Int]]                 = builder.add(Flow[Int].map(n => if (n > 0) Some(n) else None))
          val partitionShape: UniformFanOutShape[Option[Int], Option[Int]] = builder.add(
            Partition[Option[Int]](
              2,
              {
                case Some(_) => 0
                case None    => 1
              }
            )
          )
          validationShape ~> partitionShape

          UniformFanOutShape(validationShape.in, partitionShape.out(0), partitionShape.out(1))
        }

      val flow = Flow
        .fromGraph(GraphDSL.create() { implicit b =>
          import GraphDSL.Implicits._

          val workerCount = 4

          val validation = b.add(validationGraph)
          val partition  = b.add(
            Partition[Option[Int]](
              workerCount,
              {
                case Some(value) => value % workerCount
                case None        => throw new RuntimeException()
              }
            )
          )
          val merge      = b.add(Merge[Int](workerCount + 1))

          validation.out(0) ~> partition.in

          for (_ <- 1 to workerCount)
            partition ~> Flow[Option[Int]].log("partition").map(spin).async ~> merge

          validation.out(1) ~> Flow[Option[Int]].log("partition").map(_ => 0).async ~> merge

          FlowShape(validation.in, merge.out)
        })
        .withAttributes(Attributes.logLevels(onElement = LogLevels.Info))

      val testSource = TestSource.probe[Int]
      val testSink   = TestSink.probe[Int]

      val materializedTestValue   = testSource.via(flow).toMat(testSink)(Keep.both).run()
      val (publisher, subscriber) = materializedTestValue

      publisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      subscriber
        .request(3) // Don't forget this
        .expectNext(1, 5, 13)
        .expectComplete()

    }

    "monadic short-circuiting in streams" in {

      object PartitionTry {
        def apply[T]() =
          GraphDSL.create[FanOutShape2[Try[T], Throwable, T]]() { implicit builder =>
            import GraphDSL.Implicits._

            val success   = builder.add(Flow[Try[T]].collect { case Success(a) => a })
            val failure   = builder.add(Flow[Try[T]].collect { case Failure(t) => t })
            val partition = builder.add(Partition[Try[T]](2, _.fold(_ => 0, _ => 1)))

            partition ~> failure
            partition ~> success

            new FanOutShape2[Try[T], Throwable, T](partition.in, failure.out, success.out)
          }
      }

      object ErrorHandlingFlow {
        def apply[T, MatErr](errorSink: Sink[Throwable, MatErr]): Flow[Try[T], T, MatErr] =
          Flow.fromGraph(
            GraphDSL.createGraph(errorSink) { implicit builder => sink =>
              import GraphDSL.Implicits._

              val partition = builder.add(PartitionTry[T]())

              partition.out0 ~> sink

              new FlowShape[Try[T], T](partition.in, partition.out1)
            }
          )
      }

      val source: Source[String, NotUsed]                 = Source(List("1", "2", "hello"))
      val convert: Flow[String, Try[Int], NotUsed]        = Flow.fromFunction((s: String) =>
        Try {
          s.toInt
        }
      )
      val errorsSink: Sink[Throwable, Future[Done]]       = Sink.foreach[Throwable](println)
      val handleErrors: Flow[Try[Int], Int, Future[Done]] = ErrorHandlingFlow(errorsSink)

      source.via(convert).via(handleErrors).runForeach(println)

    }

    "test zipWith flows with a test source AND a test sink" in {

      final class DestinationActor extends Actor with ActorLogging {
        override def receive: Receive = {
          case 1  => sender() ! "Uno"
          case 5  => sender() ! "Cinco"
          case 13 => sender() ! "Trece"
          case _  => sender() ! "NS/NC"
        }
      }

      val destinationActor = system.actorOf(Props(new DestinationActor))

      val flow = Flow
        .fromGraph(GraphDSL.create() { implicit b =>
          import GraphDSL.Implicits._
          implicit val timeout: Timeout = Timeout(10 seconds)

          val flow      = b.add(Flow[Int].ask[String](parallelism = 4)(destinationActor))
          val broadcast = b.add(Broadcast[Int](2))
          val zipWith   = b.add(ZipWith[Int, String, String]((int, string) => s"$int.- $string"))

          broadcast ~> flow ~> zipWith.in1
          broadcast ~> zipWith.in0

          FlowShape(broadcast.in, zipWith.out)

        })
        .withAttributes(Attributes.logLevels(onElement = LogLevels.Info))

      val testSource = TestSource.probe[Int]
      val testSink   = TestSink.probe[String]

      val materializedTestValue   = testSource.via(flow).toMat(testSink)(Keep.both).run()
      val (publisher, subscriber) = materializedTestValue

      publisher
        .sendNext(1)
        .sendNext(5)
        .sendNext(13)
        .sendComplete()

      subscriber
        .request(3) // Don't forget this
        .expectNext("1.- Uno", "5.- Cinco", "13.- Trece")
        .expectComplete()

    }

    "test flowShape from sink and materializedValue" in {

      val sinkFoldFlow2: Graph[FlowShape[Int, Either[Throwable, Int]], Future[Int]] =
        GraphDSL.createGraph(Sink.fold[Int, Int](0)(_ + _)) { implicit builder => sink =>
          import GraphDSL.Implicits._
          FlowShape(sink.in, builder.materializedValue.mapAsync(4)(identity).map(Right(_)).outlet)
        }

      val sinkSeqFlow1: Graph[FlowShape[Int, Either[Throwable, Seq[Int]]], Future[Seq[Int]]] =
        GraphDSL
          .createGraph(Sink.seq[Int]) { implicit builder => sink =>
            import GraphDSL.Implicits._
            FlowShape(sink.in, builder.materializedValue.mapAsync(4)(identity).map(Right(_)).outlet)
          }

      val graph = GraphDSL
        .create() { implicit builder =>
          import GraphDSL.Implicits._

          val broadcast = builder.add(Broadcast[Int](3))
          val zipWith   = builder.add(
            ZipWith[
              Int,
              Either[Throwable, Seq[Int]],
              Either[Throwable, Int],
              Int
            ] {
              case (message, Right(_), Right(_)) => message
              case (_, _, _)                     => 0
            }
          )

          broadcast.out(0).log("broadcast(0)") ~> zipWith.in0

          broadcast
            .out(1)
            .log("broadcast(1)")
            .flatMapConcat(Source.single(_).via(sinkSeqFlow1).log("sinkFlow1")) ~> zipWith.in1

          broadcast
            .out(2)
            .log("broadcast(2)")
            .flatMapConcat(Source.single(_).via(sinkFoldFlow2).log("sinkFlow2")) ~> zipWith.in2

          FlowShape(broadcast.in, zipWith.out.log("zipWith.out").outlet)

        }

      val simpleSource = Source(1 to 10)

      val testSink              = TestSink.probe[Int]
      val materializedTestValue = simpleSource.via(graph).runWith(testSink)

      materializedTestValue
        .request(10)
        .expectNext(1, 2, 3 to 10: _*)
        .expectComplete()

    }

    "test sharedKillSwitch" in {

      val countingSrc      = Source(LazyList.from(1)).delay(1.second)
      val lastSnk          = TestSink.probe[Int]
      val sharedKillSwitch = KillSwitches.shared("my-kill-switch")

      val last1 = countingSrc.via(sharedKillSwitch.flow).runWith(lastSnk)
      val last2 = countingSrc.via(sharedKillSwitch.flow).runWith(lastSnk)

      val error = new RuntimeException("boom!")
      sharedKillSwitch.abort(error)

      last1.expectSubscriptionAndError(error)
      last2.expectSubscriptionAndError(error)

    }

    "test KillSwitches.single" in {

      import system.dispatcher
      val killSwitchFlow = KillSwitches.single[Int]
      val counter        = Source(LazyList.from(1)).throttle(1, 1 second).log("counter")
      val sink           = TestSink.probe[Int]

      val (killSwitch, materializedTestValue) = counter
        .viaMat(killSwitchFlow)(Keep.right)
        .toMat(sink)(Keep.both)
        .run()

      system.scheduler.scheduleOnce(3 seconds) {
        killSwitch.shutdown()
      }

      materializedTestValue.request(3).expectNext(1, 2, 3).expectComplete()

    }

  }

}
