package io.github.mvillafuertem.scala.akka.stream.init

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object OperatorFusion extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("FirstPrinciples")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 10)
  val simpleSink = Sink.foreach[Int](println)

  // This run on the SAME ACTOR
  simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink)

  //.run()
  // Operator/Component FUSION
  val simpleActor = actorSystem.actorOf(Props[SimpleActor])
  // Complex flows
  val complexFlow = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }
  //(1 to 1000).foreach(simpleActor ! _)
  val complexFlow2 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x * 10
  }

  // "equivalent" behavior
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case x: Int =>
        // flow operations
        val x2 = x + 1
        val y = x2 * 10
        // sink operation
        println(y)
    }
  }

  // This not use async boundary because runs on one actor very slowly
  // simpleSource.via(complexFlow).via(complexFlow2).to(simpleSink).run()

  // Async boundary
  simpleSource.via(complexFlow).async // runs on one actor
    .via(complexFlow2).async // runs on a second actor
    .to(simpleSink) // runs on a third actor
  //.run()

  // Ordering guarantees without async
  Source(1 to 3)
    .map(element => {
      println(s"Flow A: $element"); element
    }) //.async
    .map(element => {
    println(s"Flow B: $element"); element
  }) //.async
    .map(element => {
    println(s"Flow C: $element"); element
  }) //.async
    .runWith(Sink.ignore)


}
