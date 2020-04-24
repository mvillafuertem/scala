package io.github.mvillafuertem.akka.untyped.stream.graph

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip }
import akka.stream.{ ClosedShape, Materializer }

import scala.concurrent.duration._

object GraphBasics extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("GraphBasics")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  val input       = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1)
  val multiplier  = Flow[Int].map(x => x * 10)
  val output      = Sink.foreach[(Int, Int)](println)

  // Step 1 - Setting up the fundamentals for the graph
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      // Step 2 - Add the necessary components of this graph
      val broadcast = builder.add(Broadcast[Int](2)) // Fan-Out operator
      val zip       = builder.add(Zip[Int, Int])     // Fan-In operator

      // Step 3 - Tying up the components
      input ~> broadcast
      broadcast.out(0) ~> incrementer ~> zip.in0
      broadcast.out(1) ~> multiplier ~> zip.in1

      zip.out ~> output

      // Step 4 - Return a closed shape
      ClosedShape
    }
  )
  // graph.run()

  /**
   * Feed a source into 2 sinks at the same time (hint: use a broadcast)
   */
  val firstSink  = Sink.foreach[Int](x => println(s"First sink: $x"))
  val secondSink = Sink.foreach[Int](x => println(s"Second sink: $x"))

  // Step 1 - Setting up the fundamentals for the graph
  val sourceToTwoSinksGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring the components
      val broadcast = builder.add(Broadcast[Int](2))

      // Step 3 - tying up the components
      input ~> broadcast ~> firstSink // Use of implicit port numbering
      broadcast ~> secondSink
      // "Equivalent"
      // broadcast.out(0) ~> firstSink
      // broadcast.out(1) ~> secondSink

      // Step 4 - Return a closed shape
      ClosedShape
    }
  )

  /**
   * Balance
   */
  val fastSource = input.throttle(5, 1 second)
  val slowSource = input.throttle(2, 1 second)

  val sink1 = Sink.fold[Int, Int](0) { (count, element) =>
    println(s"Sink 1 number of elements: $count")
    count + 1
  }

  val sink2 = Sink.fold[Int, Int](0) { (count, element) =>
    println(s"Sink 2 number of elements: $count")
    count + 1
  }

  // Step 1 - Setting up the fundamentals for the graph
  val balanceGraph = RunnableGraph
    .fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        // Step 2 - Declaring the components
        val merge   = builder.add(Merge[Int](2))
        val balance = builder.add(Balance[Int](2))

        // Step 3 - Tie them up
        fastSource ~> merge ~> balance ~> sink1
        fastSource ~> merge
        balance ~> sink2

        // Step 4 - Return a closed shape
        ClosedShape
      }
    )
    .run()

}
