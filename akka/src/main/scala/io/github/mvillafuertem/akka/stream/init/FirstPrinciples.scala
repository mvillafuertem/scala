package io.github.mvillafuertem.akka.stream.init

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object FirstPrinciples extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("FirstPrinciples")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // Producer
  val source = Source(1 to 10)

  // Consumer
  val sink = Sink.foreach[Int](println)

  // Graph
  val graph = source.to(sink)
  graph.run()

  // Transform Element
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val flowWithSink = flow.to(sink)

  sourceWithFlow.to(sink).run()
  source.to(flowWithSink).run()
  source.via(flow).to(sink).run()

  // Kind of Producers
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1, 2, 3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1))
  val futureSource = Source.fromFuture(Future(42))

  // Kind of Consumer
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int]
  val foldSink = Sink.fold[Int, Int](0)((a, b) => a + b)

  // Kind of Flow
  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(5)

  // Pipes
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  // Syntactic Sugar

  // Source(1 to 10).via(Flow[Int].map(x => x * 2)).run()
  val mapSource = Source(1 to 10).map(x => x * 2)
  // mapSource.to(Sink.foreach[Int](println)).run()
  mapSource.runForeach(println)

  // Operators = Components

  /**
    * Stream that takes the names of persons,
    * then you will keep the first 2 names with
    * length > 5 characters
    */
  val namesOfPersonsSource = Source(List("Amanda", "Miranda", "Cassandra"))
  val businessRuleFlow = Flow[String].take(2).filter(name => name.length > 5)
  val namesOfPersonsSink = Sink.foreach[String](println)

  // Without Syntactic Sugar
  namesOfPersonsSource.via(businessRuleFlow).to(namesOfPersonsSink).run()
  // With Syntactic Sugar
  namesOfPersonsSource.take(2).filter(_.length > 5).runForeach(println)

}
