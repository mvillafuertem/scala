package io.github.mvillafuertem.akka.untyped.stream.graph

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith }
import akka.stream.{ ClosedShape, FanOutShape2, Materializer, UniformFanInShape }

object ComplexOpenGraphs extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("ComplexOpenGraphs")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

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
  ) //.run()

  // Same for UniformFanOutShape
  /**
   * Non-uniform Fan Out Shape
   *
   * Processing bank transactions
   * Tx suspicious if amount > 10000
   *
   * Streams component for Tx
   *
   * - Output1 ~ Let the transaction go through
   * - Output2 ~ Suspicious Tx ids
   */
  case class Transaction(id: String, source: String, recipient: String, amount: Int, date: Date)

  val transactionSource = Source(
    List(
      Transaction("1234567890", "Pepe", "Juan", 100, new Date),
      Transaction("0987654321", "Pedro", "Juan", 10001, new Date),
      Transaction("6789012345", "Maria", "Ana", 100, new Date)
    )
  )

  val bankProcessor             = Sink.foreach[Transaction](println)
  val suspiciousAnalysisService = Sink.foreach[String](txId => println(s"Suspicious transaction ID $txId"))

  // Step 1 - Setting up the fundamentals for the graph
  val suspiciousTxStaticGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    // Step 2 - Declaring auxiliary shapes
    val broadcast          = builder.add(Broadcast[Transaction](2))
    val suspiciousTxFilter = builder.add(Flow[Transaction].filter(tx => tx.amount > 10000))
    val txIdExtractor      = builder.add(Flow[Transaction].map[String](tx => tx.id))

    // Step 3 - tying up the shapes
    broadcast.out(0) ~> suspiciousTxFilter ~> txIdExtractor

    // Step 4 - Return a shape
    new FanOutShape2(broadcast.in, broadcast.out(1), txIdExtractor.out)
  }

  val suspiciousTRunnableGraph = RunnableGraph
    .fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        // Step 2 - Declaring auxiliary shapes
        val suspiciousTxShape = builder.add(suspiciousTxStaticGraph)

        // Step 3 - tying up the shapes
        transactionSource ~> suspiciousTxShape.in
        suspiciousTxShape.out0 ~> bankProcessor
        suspiciousTxShape.out1 ~> suspiciousAnalysisService

        // Step 4 - Return a shape
        ClosedShape
      }
    )
    .run()

}
