package io.github.mvillafuertem.akka.untyped.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Partition, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.stream.{ActorMaterializer, FanOutShape2, FlowShape}
import akka.testkit.{TestKit, TestProbe}
import akka.{Done, NotUsed}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * @author Miguel Villafuerte
 */
final class TestingStreamsSpec extends TestKit(ActorSystem("TestingStreams"))
  with WordSpecLike
  with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A simple stream" should {

    "satisfy basic assertions" in {
      // describe our test

      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold(0)((a: Int, b: Int) => a + b)

      val sumFuture = simpleSource.toMat(simpleSink)(Keep.right).run()
      val sum = Await.result(sumFuture, 2 seconds)

      assert(sum == 55)
    }

    "integrate with test actors via materialized values" in {

      import akka.pattern.pipe
      import system.dispatcher

      val simpleSource = Source(1 to 10)
      val simpleSink = Sink.fold(0)((a: Int, b: Int) => a + b)

      val probe = TestProbe()

      simpleSource.toMat(simpleSink)(Keep.right).run().pipeTo(probe.ref)

      probe.expectMsg(55)
    }

    "integrate with test-actor-based sink" in {

      val simpleSource = Source(1 to 5)
      // 0, 1, 3, 6, 10, 15
      val flow = Flow[Int].scan[Int](0)(_ + _)
      val streamUnderTest = simpleSource.via(flow)

      val probe = TestProbe()
      val probeSink = Sink.actorRef(probe.ref, "completionMessage")

      streamUnderTest.to(probeSink).run()

      probe.expectMsgAllOf(0, 1, 3, 6, 10, 15)
    }

    "integrate with streams testkit sink" in {

      val sourceUnderTest = Source(1 to 5).map(_ * 2)

      val testSink = TestSink.probe[Int]
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
        case _ =>
      }

      val testSource = TestSource.probe[Int]
      val materializedTestValue = testSource.toMat(sinkUnderTest)(Keep.both).run()
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
      val testSink = TestSink.probe[Int]

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

    "monadic short-circuiting in streams" in {


      object PartitionTry {
        def apply[T]() = GraphDSL.create[FanOutShape2[Try[T], Throwable, T]]() { implicit builder =>
          import GraphDSL.Implicits._

          val success = builder.add(Flow[Try[T]].collect { case Success(a) => a })
          val failure = builder.add(Flow[Try[T]].collect { case Failure(t) => t })
          val partition = builder.add(Partition[Try[T]](2, _.fold(_ => 0, _ => 1)))

          partition ~> failure
          partition ~> success

          new FanOutShape2[Try[T], Throwable, T](partition.in, failure.out, success.out)
        }
      }

      object ErrorHandlingFlow {
        def apply[T, MatErr](errorSink: Sink[Throwable, MatErr]): Flow[Try[T], T, MatErr] = Flow.fromGraph(
          GraphDSL.create(errorSink) { implicit builder => sink =>
            import GraphDSL.Implicits._

            val partition = builder.add(PartitionTry[T]())

            partition.out0 ~> sink

            new FlowShape[Try[T], T](partition.in, partition.out1)
          }
        )
      }

      val source      : Source[String, NotUsed]           = Source(List("1", "2", "hello"))
      val convert     : Flow[String, Try[Int], NotUsed]   = Flow.fromFunction((s: String) => Try{s.toInt})
      val errorsSink  : Sink[Throwable, Future[Done]]     = Sink.foreach[Throwable](println)
      val handleErrors: Flow[Try[Int], Int, Future[Done]] = ErrorHandlingFlow(errorsSink)

      source.via(convert).via(handleErrors).runForeach(println)

    }

  }

}
