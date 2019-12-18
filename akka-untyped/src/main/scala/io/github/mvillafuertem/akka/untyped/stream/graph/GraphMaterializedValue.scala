package io.github.mvillafuertem.akka.untyped.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{FlowShape, Materializer, SinkShape}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object GraphMaterializedValue extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("GraphMaterializedValue")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  val wordSource = Source(List("Akka", "learning", "awesome"))
  val printer = Sink.foreach[String](println)
  val counter = Sink.fold[Int, String](0)((count, _) => count + 1)

  /**
    * A Composite component (Sink)
    * - Print out all strings which are lowercase
    * - Counts the strings that are short (< 5 chars)
    */

  // Step 1 - Setting up the fundamentals for the graph
  val complexWordSink = Sink.fromGraph(
    GraphDSL.create(printer, counter)((printerMatValue, counterMatValue) => counterMatValue) {
      implicit builder => (printerShape, counterShape) =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring the components
      val broadcast = builder.add(Broadcast[String](2))
      val lowercaseFilter = builder.add(Flow[String].filter(word => word == word.toLowerCase()))
      val shortStringFilter = builder.add(Flow[String].filter(_.length < 5))

      // Step 3 - tying up the components
      broadcast ~> lowercaseFilter ~> printerShape
      broadcast ~> shortStringFilter ~> counterShape

      // Step 4 - Return a shape
      SinkShape(broadcast.in)

    }
  )

  import actorSystem.dispatcher

  val shortStringsCountFuture = wordSource.toMat(complexWordSink)(Keep.right).run()
  shortStringsCountFuture.onComplete {
    case Success(count) => println(s"The total number of short strings is $count")
    case Failure(exception) => println(s"The count of short strins failed $exception")
  }


  /**
    *  Exercise
    */

  def enhanceFlow[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = {

    val counterSink = Sink.fold[Int, B](0)((count, _) => count + 1)

    Flow.fromGraph(
      GraphDSL.create(counterSink) { implicit builder => counterSinkShape =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[B](2))
        val originalFlowShape = builder.add(flow)

        originalFlowShape ~> broadcast ~> counterSinkShape

        FlowShape(originalFlowShape.in, broadcast.out(1))

      }
    )
  }

  val simpleSource = Source(1 to 42)
  val simpleFlow = Flow[Int].map(x => x)
  val simpleSink = Sink.ignore

  val enhancedFlowCountFuture = simpleSource.viaMat(enhanceFlow(simpleFlow))(Keep.right).toMat(simpleSink)(Keep.left).run()

  enhancedFlowCountFuture.onComplete {
    case Success(count) => println(s"$count elements went through the enhanced flow")
    case _ => println(s"Something failed")
  }


}
