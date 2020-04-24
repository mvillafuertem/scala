package io.github.mvillafuertem.akka.untyped.stream.techniques

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.{ Resume, Stop }
import akka.stream.scaladsl.{ Broadcast, GraphDSL, RestartSource, RunnableGraph, Sink, Source, Zip }
import akka.stream.{ ActorAttributes, ClosedShape, Materializer }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object FaultTolerance extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("FaultTolerance")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  // 1. Logging
  val faultySource = Source(1 to 10).map(e => if (e == 6) throw new RuntimeException else e)
  faultySource
    .log("trackingElements")
    .to(Sink.ignore)
  //.run()

  // 2. Gracefully terminating a stream
  faultySource.recover {
    case _: RuntimeException => Int.MinValue
  }.log("gracefulSource")
    .to(Sink.ignore)
  //.run()

  // 3. Recover with another stream
  faultySource
    .recoverWithRetries(3, {
      case _: RuntimeException => Source(90 to 99)
    })
    .log("recoverWithRetries")
    .to(Sink.ignore)
  //.run()

  // 4. Backoff supervision
  val restartSource = RestartSource.onFailuresWithBackoff(
    minBackoff = 1 second,
    maxBackoff = 30 second,
    randomFactor = 0.2
  ) { () =>
    val randomNumber = new Random().nextInt(20)
    Source(1 to 10).map(elem => if (elem == randomNumber) throw new RuntimeException else elem)
  }

  restartSource
    .log("restartBackoff")
    .to(Sink.ignore)
  //.run()

  // 5. Supervision strategy
  val numbers = Source(1 to 20)
  //.map(n => if (n == 13) throw new RuntimeException("bad luck") else n)
    .mapAsync(1)(n => if (n == 13) Future.failed(new RuntimeException("bad luck")) else Future.successful(n))
  //.log("supervision")

  val supervisedNumbers = numbers
//    .withAttributes(ActorAttributes.supervisionStrategy {
//    // Resume = skips the faulty element
//    // Stop = stop the stream
//    // Restart = resume + clears internal state
//
//    case e: RuntimeException =>
//      println(e.getMessage)
//      Resume
//    case _ => Stop
//  })

  //supervisedNumbers.to(Sink.ignore).run()

  val output = Sink.foreach[(Int, Int)](println)

  val graph = RunnableGraph
    .fromGraph(
      GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        // Step 2 - Add the necessary components of this graph
        val broadcast = builder.add(Broadcast[Int](2)) // Fan-Out operator
        val zip       = builder.add(Zip[Int, Int])     // Fan-In operator

        // Step 3 - Tying up the components
        supervisedNumbers ~> broadcast
        broadcast.out(0) ~> zip.in0
        broadcast.out(1) ~> zip.in1

        zip.out ~> output

        // Step 4 - Return a closed shape
        ClosedShape
      }
    )
    .withAttributes(ActorAttributes.supervisionStrategy {
      // Resume = skips the faulty element
      // Stop = stop the stream
      // Restart = resume + clears internal state

      case e: RuntimeException =>
        println(e.getMessage)
        Resume
      case _ => Stop
    })
    .run()

}
