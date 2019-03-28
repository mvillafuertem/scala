package io.github.mvillafuertem.scala.akka.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}

object ComplexOpenGraphs extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("GraphBasics")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  /**
   * Max3 Operator
   * - 3 Inputs of type Int
   * - The maximum of the 3
   */
  // Step 1 - Setting up the fundamentals for the graph
  val max3StaticGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    // Step 2 - Declaring auxiliary shapes
    val max1 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))
    val max2 = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))

    // Step 3 - tying up the shapes
    max1.out ~> max2.in0

    // Step 4 - Return a shape
    UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source((1 to 10).map(_ => 5))
  val source3 = Source((1 to 10).reverse)

  val maxSink = Sink.foreach[Int](x => println(s"Max is $x"))

  // Step 1 - Setting up the fundamentals for the graph
  val max3RunnableGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Step 2 - Declaring auxiliary shapes
      val max3Shape = builder.add(max3StaticGraph)

      // Step 3 - tying up the shapes
      source1 ~> max3Shape.in(0)
      source2 ~> max3Shape.in(1)
      source3 ~> max3Shape.in(2)
      max3Shape.out ~> maxSink

      // Step 4 - Return a shape
      ClosedShape
    }
  )

  //max3RunnableGraph.run()


}
