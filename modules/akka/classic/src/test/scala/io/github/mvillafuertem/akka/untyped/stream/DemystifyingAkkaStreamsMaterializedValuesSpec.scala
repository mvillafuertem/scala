package io.github.mvillafuertem.akka.untyped.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Keep, RunnableGraph, Sink, Source }
import akka.stream.stage.{ AsyncCallback, GraphStageLogic, GraphStageWithMaterializedValue, OutHandler }
import akka.stream.{ Attributes, KillSwitch, KillSwitches, Materializer, Outlet, SourceShape }
import akka.testkit.TestKit
import akka.{ Done, NotUsed }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContextExecutor, Future, Promise }
import scala.util.{ Failure, Success }

// https://nivox.github.io/posts/akka-stream-materialized-values/
// https://blog.rockthejvm.com/the-brilliance-of-materialized-values/
// https://gist.github.com/mvillafuertem/a003b7f38a4fd51dcf2446ae304dcbe5
final class DemystifyingAkkaStreamsMaterializedValuesSpec extends TestKit(ActorSystem("TestingStreams")) with AnyFlatSpecLike with BeforeAndAfterAll {

  implicit val materializer: Materializer           = Materializer(system)
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  ignore should "why are MaterializedValues needed?" in {

    val promise                        = Promise[Done]()
    val stream: RunnableGraph[NotUsed] = Source
      .repeat("Hello world")
      .take(3)
      .to {
        Sink.foreach(println).mapMaterializedValue { done =>
          done.onComplete {
            case Success(_)  => promise.success(Done)
            case Failure(ex) => promise.failure(ex)
          }
        }
      }
    stream.run()

    val streamDone: Future[Done] = promise.future
    streamDone.onComplete {
      case Failure(exception) => println(exception)
      case Success(value)     => println(value)
    }
    stream.run()
    // Fail
    streamDone.onComplete {
      case Failure(exception) => println(exception)
      case Success(value)     => println(value)
    }

  }

  ignore should "how can we return our own values?" in {

    trait ControlInterface {
      def stop(): Unit
    }

    class ControlInterfaceImpl(killSwitch: KillSwitch) extends ControlInterface {
      def stop(): Unit =
        // println("valuevaluevaluevalue")
        killSwitch.shutdown()
    }

    val source: Source[Int, ControlInterfaceImpl] =
      Source
        .fromIterator(() => Iterator.from(1))
        .throttle(1, 500.millis)
        .log("Produce")
        .viaMat(KillSwitches.single[Int])(Keep.right)
        .mapMaterializedValue(s => new ControlInterfaceImpl(s))

    val value = Await.result(source.runWith(Sink.seq[Int]), 1.seconds)

    println(value)

  }

  ignore should "GraphStageWithMaterializedValue" in {

    trait ControlInterface {
      def stop(): Unit
    }

    class AsyncCallbackControlInterface(callback: AsyncCallback[Unit]) extends ControlInterface {
      def stop(): Unit =
        callback.invoke(())
    }

    class StoppableIntSource(from: Int)
        extends GraphStageWithMaterializedValue[
          SourceShape[Int],
          ControlInterface
        ] {
      val out: Outlet[Int] = Outlet("out")

      def shape: SourceShape[Int] = SourceShape(out)

      class StoppableIntSourceLogic(_shape: Shape) extends GraphStageLogic(shape) {
        private[StoppableIntSource] val stopCallback: AsyncCallback[Unit] =
          getAsyncCallback[Unit]((_) => completeStage())

        private var next: Int = from

        setHandler(
          out,
          new OutHandler {
            def onPull(): Unit = {
              push(out, next)
              next += 1
            }
          }
        )
      }

      def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, ControlInterface) = {
        val logic = new StoppableIntSourceLogic(shape)

        val controlInterface =
          new AsyncCallbackControlInterface(logic.stopCallback)

        logic -> controlInterface
      }
    }

    val source: Source[Int, ControlInterface] =
      Source
        .fromGraph(new StoppableIntSource(1))
        .throttle(1, 500.millis)
        .log("asdfasdf")

    val value = Await.result(source.runWith(Sink.seq[Int]), 60.seconds)

    println(value)

    trait AmazingLibrary {
      def complexComputation(source: Source[Int, _]): Future[Int]
    }

    val (control: ControlInterface, linkedSource: Source[Int, NotUsed]) =
      source.preMaterialize()

    val amazingLibrary: AmazingLibrary = ???
    val resutlF: Future[Int]           =
      amazingLibrary.complexComputation(linkedSource)

  }

}
