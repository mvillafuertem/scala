package io.github.mvillafuertem.akka.untyped.stream.graph

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Source, Zip}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ReusableGraph extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("ReusableGraph")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()


  private val source: Source[String, NotUsed] = Source.single("Hello World")
  private val bizLogic: Flow[String, String, NotUsed] = Flow[String].map(s => s.toUpperCase())
  private val sink: Sink[Any, Future[Done]] = Sink.foreach(println)

  private val eventualDone: Future[Done] = source
    .via(bizLogic)
    .toMat(sink)(Keep.right)
    .run()

  eventualDone.onComplete {
    case Success(Done) =>
      println("Stream finished successfully.")
    case Failure(e) =>
      println(s"Stream failed with $e")
  }


  val pairUpWithToString =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // prepare graph elements
      val broadcast = b.add(Broadcast[Int](2))
      val zip = b.add(Zip[Int, String]())

      // connect the graph
      broadcast.out(0).map(identity) ~> zip.in0
      broadcast.out(1).map(_.toString) ~> zip.in1

      // expose ports
      FlowShape(broadcast.in, zip.out)
    })

  pairUpWithToString.runWith(Source(List(1)), sink)


  val f1 = Flow[Int].map(_ + 10)
  val f2 = Flow[Int].map(_ + 10)
  val f3 = Flow[Int].map(_ + 10)
  val f4 = Flow[Int].map(_ + 10)

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val in = Source(1 to 10)
    val out = sink

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))



    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
                bcast ~> f4 ~> merge

    ClosedShape
  })

  val sharedDoubler = Flow[Int].map(_ * 2)

  RunnableGraph.fromGraph(GraphDSL.create(sink, sink)((_, _)) { implicit builder =>
    (topHS, bottomHS) =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Int](2))
      Source.single(1) ~> broadcast.in

      broadcast ~> sharedDoubler ~> topHS.in
      broadcast ~> sharedDoubler ~> bottomHS.in
      ClosedShape
  }).run()

}
