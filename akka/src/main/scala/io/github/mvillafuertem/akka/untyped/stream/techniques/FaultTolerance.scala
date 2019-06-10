package io.github.mvillafuertem.akka.untyped.stream.techniques

import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{RestartSource, Sink, Source}

import scala.concurrent.duration._
import scala.util.Random

object FaultTolerance extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("FaultTolerance")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // 1. Logging
  val faultySource = Source(1 to 10).map(e => if(e == 6) throw new RuntimeException else e)
  faultySource
    .log("trackingElements")
    .to(Sink.ignore)
    //.run()

  // 2. Gracefully terminating a stream
  faultySource.recover {
    case _: RuntimeException => Int.MinValue
  } .log("gracefulSource")
    .to(Sink.ignore)
    //.run()

  // 3. Recover with another stream
  faultySource.recoverWithRetries(3, {
    case _: RuntimeException => Source(90 to 99)
  }).log("recoverWithRetries")
    .to(Sink.ignore)
    //.run()

  // 4. Backoff supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 1 second,
    maxBackoff = 30 second,
    randomFactor = 0.2
  )(() => {
    val randomNumber = new Random().nextInt(20)
    Source(1 to 10).map(elem => if (elem == randomNumber) throw new RuntimeException else elem)
  })

  restartSource.log("restartBackoff")
    .to(Sink.ignore)
    //.run()

  // 5. Supervision strategy
  val numbers = Source(1 to 20)
    .map(n => if (n == 13) throw new RuntimeException("bad luck") else n)
    .log("supervision")

  val supervisedNumbers = numbers.withAttributes(ActorAttributes.supervisionStrategy {
    // Resume = skips the faulty element
    // Stop = stop the stream
    // Restart = resume + clears internal state

    case _: RuntimeException => Resume
    case _ => Stop
  })

  supervisedNumbers.to(Sink.ignore).run()


}
