package io.github.mvillafuertem.akka.untyped.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Sink, Source, Zip }
import akka.stream.{ ClosedShape, Materializer, OverflowStrategy, UniformFanInShape }

object GraphCycles extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("BidirectionalFlows")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  val accelerator        = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape      = builder.add(Source(1 to 100))
    val mergeShape       = builder.add(Merge[Int](2))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"Accelerating $x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrementerShape
    mergeShape <~ incrementerShape
    ClosedShape
  }

  // Graph cycle deadlock!
  // RunnableGraph.fromGraph(accelerator).run()

  /**
   *  Solution 1: MergePreferred
   */
  val actualAccelerator  = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape      = builder.add(Source(1 to 100))
    val mergeShape       = builder.add(MergePreferred[Int](1))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"Accelerating $x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrementerShape
    mergeShape.preferred <~ incrementerShape
    ClosedShape
  }

  //RunnableGraph.fromGraph(actualAccelerator).run()

  /**
   *  Solution 2: Buffer
   */
  val bufferedRepeater   = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape   = builder.add(Source(1 to 100))
    val mergeShape    = builder.add(Merge[Int](2))
    val repeaterShape = builder.add(Flow[Int].buffer(10, OverflowStrategy.dropHead).map { x =>
      println(s"Accelerating $x")
      Thread.sleep(100)
      x
    })

    sourceShape ~> mergeShape ~> repeaterShape
    mergeShape <~ repeaterShape
    ClosedShape
  }

  // RunnableGraph.fromGraph(bufferedRepeater).run()

  // Cycles risk deadlocking
  // Add bounds to the number of elements in the cycle
  // Boundedness vs Liveness

  /**
   * Challenge
   * Create a fan-in shape
   * - Two inputs which will be fed with EXACTLY ONE number
   * - Output will emit an INFINITE FIBONACCI SEQUENCE based off those 2 numbers
   *
   * 1, 3, 3, 5, 8 ...
   *
   * Hint: Use ZipWith and cycles, MergePreferred
   */
  val fibonacciGenerator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip            = builder.add(Zip[BigInt, BigInt]())
    val mergePreferred = builder.add(MergePreferred[(BigInt, BigInt)](1))
    val fibonacciLogic = builder.add(Flow[(BigInt, BigInt)].map { pair =>
      val last     = pair._1
      val previous = pair._2

      Thread.sleep(100)

      (last + previous, last)
    })

    val broadcast   = builder.add(Broadcast[(BigInt, BigInt)](2))
    val extractLast = builder.add(Flow[(BigInt, BigInt)].map(_._1))

    zip.out ~> mergePreferred ~> fibonacciLogic ~> broadcast ~> extractLast
    mergePreferred.preferred <~ broadcast

    UniformFanInShape(extractLast.out, zip.in0, zip.in1)
  }

  val fibonacciGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val source1   = builder.add(Source.single[BigInt](1))
      val source2   = builder.add(Source.single[BigInt](1))
      val sink      = builder.add(Sink.foreach[BigInt](println))
      val fibonacci = builder.add(fibonacciGenerator)

      source1 ~> fibonacci.in(0)
      source2 ~> fibonacci.in(1)
      fibonacci.out ~> sink

      ClosedShape
    }
  )

  fibonacciGraph.run()

}
