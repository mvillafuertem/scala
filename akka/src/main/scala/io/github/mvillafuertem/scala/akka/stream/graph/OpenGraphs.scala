package io.github.mvillafuertem.scala.akka.stream.graph

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}

object OpenGraphs extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("GraphBasics")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  /**
   * A Composite source that concatenates 2 sources
   * - Emits ALL the elements from the first sources
   * - Then All the elements from second
   */
  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  // Step 1 - Setting up the fundamentals for the graph
  val sourceGraph = Source.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring the components
      val concat = builder.add(Concat[Int](2))

      // Step 3 - tying up the components
      firstSource ~> concat
      secondSource ~> concat

      // Step 4 - Return a shape
      SourceShape(concat.out)

    }
  )//.to(Sink.foreach(println)).run()

  /**
    * Complex Sink
    */
  val firstSink = Sink.foreach[Int](x => println(s"Meaningful thin 1: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Meaningful thin 2: $x"))

  // Step 1 - Setting up the fundamentals for the graph
  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring the components
      val broadcast = builder.add(Broadcast[Int](2))

      // Step 3 - tying up the components
      broadcast ~> firstSink
      broadcast ~> secondSink

      // Step 4 - Return a shape
      SinkShape(broadcast.in)
    }
  )

  // firstSource.to(sinkGraph).run()


  /**
    * Complex Flow
    * Write your ouw flow that is composed of two others flows
    * - One that adds 1 to a number
    * - One that does number * 10
    */

  val incrementer = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_* 10)

  // Step 1 - Setting up the fundamentals for the graph
  val flowGraph = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring the shapes
      val incrementerShape = builder.add(incrementer)
      val multiplierShape = builder.add(multiplier)

      // Step 3 - tying up the shapes
      incrementerShape ~> multiplierShape

      // Step 4 - Return a shape
      FlowShape(incrementerShape.in, multiplierShape.out)
    }
  )

  //firstSource.via(flowGraph).to(Sink.foreach(println)).run()

  /**
    * Flow from a Sink and a Source?
    */
  def fromSinkAndSource[A, B](sink: Sink[A, _], source: Source[B, _]): Flow[A, B, _] =
    // Step 1 - Setting up the fundamentals for the graph
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>

        // Step 2 - Declaring the shapes
        val sourceShape = builder.add(source)
        val sinkShape = builder.add(sink)

        // Step 3 - tying up the shapes
        // Step 4 - Return a shape
        FlowShape(sinkShape.in, sourceShape.out)
      }
    )

  private val f: Flow[String, Int, NotUsed] = Flow.fromSinkAndSourceCoupled(Sink.foreach[String](println), Source(1 to 10))

}
