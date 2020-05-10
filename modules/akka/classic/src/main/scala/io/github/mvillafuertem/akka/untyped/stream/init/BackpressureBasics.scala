package io.github.mvillafuertem.akka.untyped.stream.init

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ Materializer, OverflowStrategy }

import scala.concurrent.duration._

object BackpressureBasics extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("BackpressureBasics")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  val fastSource = Source(1 to 1000)
  val slowSink   = Sink.foreach[Int] { x =>
    // Simulate a long processing
    Thread.sleep(1000)
    println(s"Sink $x")
  }

  // This is not Backpressure
  // fastSource.to(slowSink).run() // fusing ?!

  // This is Backpressure
  fastSource.async.to(slowSink) //.run()

  val simpleFlow = Flow[Int].map { x =>
    println(s"Incoming $x")
    x + 1
  }

  fastSource.async
    .via(simpleFlow)
    .async
    .to(slowSink)
  //.run()

  // Reactions to Backpressure (in order)
  // - Try to slow down if possible
  // - Buffer elements until there is more demand
  // - Drop down elements from the buffer if it overflows
  // - Tear donw/kill the whole stream (failure)

  /**
   * 1  - 16 nobody is backpressured
   * 17 - 26 flow will buffer, flow will start dropping at the next element
   * 26 - 1000 flow will always drop the oldest element
   */
  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  fastSource.async
    .via(bufferedFlow)
    .async
    .to(slowSink)
  //.run()

  // Overflow Strategies
  // - Drop Head = oldest
  // - Drop Tail = newest
  // - Drop New = exact element to be added = keeps the buffer
  // - Drop the entire buffer
  // - Backpressure signal
  // - Fail

  // Throttling
  fastSource.throttle(5, 1 second).runWith(Sink.foreach(println))
}
