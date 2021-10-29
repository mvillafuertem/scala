package io.github.mvillafuertem.akka.untyped.stream.techniques

import akka.Done
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.stream.{ CompletionStrategy, Materializer, OverflowStrategy }
import akka.util.Timeout

import scala.concurrent.duration._

object IntegratingWithActors extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("IntegratingWithActors")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  class SimpleActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case s: String =>
        log.info(s"Just received a string: $s")
        sender() ! s"$s$s"
      case n: Int    =>
        log.info(s"Just received a number: $n")
        sender() ! (2 * n)
      case _         =>
    }

  }

  val simpleActor = actorSystem.actorOf(Props[SimpleActor](), "simpleActor")

  val numberSource = Source(1 to 10)

  // Actor as a Flow
  implicit val timeout: Timeout = Timeout(1 seconds)
  val actorBasedFlow            = Flow[Int].ask(parallelism = 4)(simpleActor)

  // numberSource.via(actorBasedFlow).to(Sink.ignore).run()
  // Equivalent ^
  // numberSource.ask[Int](parallelism = 4)(simpleActor).to(Sink.ignore).run()

  private val completionMatcher: PartialFunction[Any, CompletionStrategy] = { case Done =>
    // complete stream immediately if we send it Done
    CompletionStrategy.immediately
  }
  // Actor as a Source
  val actorPoweredSource                                                  = Source.actorRef[Int](
    completionMatcher,
    // never fail the stream because of a message
    failureMatcher = PartialFunction.empty,
    bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropHead
  )
  val materializedActorRef                                                = actorPoweredSource.to(Sink.foreach[Int](number => println(s"Actor powered flow got number: $number"))).run()
  materializedActorRef ! 10

  // Actor as a Destination/Sink
  // - An init message
  // - An ack message to confirm the reception
  // - A complete message
  // - A function to generate a message in case the stream throws an exception

  case object StreamInit
  case object StreamAck
  case object StreamComplete
  case class StreamFail(exception: Throwable)

  class DestinationActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case StreamInit            =>
        log.info("Stream initialized")
        sender() ! StreamAck
      case StreamComplete        =>
        log.info("Stream complete")
        context.stop(self)
      case StreamFail(exception) =>
        log.warning(s"Stream failed $exception")
        context.stop(self)
      case message               =>
        log.info(s"Message $message has come to its final resting point")
        sender() ! StreamAck
    }
  }

  val destinationActor = actorSystem.actorOf(Props[DestinationActor](), "destinationActor")
  val actorPoweredSink = Sink.actorRefWithBackpressure[Int](
    destinationActor,
    onInitMessage = StreamInit,
    onCompleteMessage = StreamComplete,
    ackMessage = StreamAck,
    onFailureMessage = throwable => StreamFail(throwable) // Optional
  )

  Source(1 to 10).to(actorPoweredSink).run()

  // Sink.actorRef() not recommended, unable to backpressure

}
