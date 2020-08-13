package io.github.mvillafuertem.akka.untyped.stream.techniques

import java.util.Date

import akka.Done
import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.dispatch.MessageDispatcher
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout

import scala.concurrent.Future

object IntegratingWithExternalServices extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("IntegratingWithExternalServices")

  // Not recommended in practice for mapAsync
  // import actorSystem.dispatcher
  implicit val dispatcher: MessageDispatcher = actorSystem.dispatchers.lookup("dedicated-dispatcher")

  def genericExtService[A, B](element: A): Future[A] = ???

  // Example: Simplified PagerDuty
  case class PagerEvent(application: String, description: String, date: Date)

  val eventSource = Source(
    List(
      PagerEvent("AkkaInfra", "Infrastructure Boke", new Date),
      PagerEvent("FastDataPipeline", "Illegal elements in the data pipeline", new Date),
      PagerEvent("AkkaInfra", "Infrastructure Boke", new Date),
      PagerEvent("SuperFrontend", "A button doesn't work", new Date)
    )
  )

  object PagerService {
    private val engineers = List("Daniel", "John", "Pepe")
    private val emails    = Map(
      "Daniel" -> "daniel@email.com",
      "John"   -> "john@email.com",
      "Pepe"   -> "pepe@email.com"
    )

    def processEvent(pagerEvent: PagerEvent) =
      Future {
        val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
        val engineer      = engineers(engineerIndex.toInt)
        val engineerEmail = emails(engineer)

        // Page the Engineer
        println(
          s"Sending engineer $engineerEmail a high priority notification $pagerEvent"
        )
        Thread.sleep(1000)

        // Return the email that was paged
        engineerEmail
      }

  }

  val infraEvents         = eventSource.filter(_.application == "AkkaInfra")
  val pagedEngineerEmails = infraEvents.mapAsync(parallelism = 1)(event => PagerService.processEvent(event))

  // Guarantees the relative order of elements
  val pagedEmailsSink: Sink[String, Future[Done]] = Sink.foreach[String](email => println(s"Successfully sent notification to $email"))

  //pagedEngineerEmails.to(pagedEmailsSink).run()

  class PagerActor extends Actor with ActorLogging {
    private val engineers = List("Daniel", "John", "Pepe")
    private val emails    = Map(
      "Daniel" -> "daniel@email.com",
      "John"   -> "john@email.com",
      "Pepe"   -> "pepe@email.com"
    )

    def processEvent(pagerEvent: PagerEvent) =
      Future {
        val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
        val engineer      = engineers(engineerIndex.toInt)
        val engineerEmail = emails(engineer)

        // Page the Engineer
        log.info(
          s"Sending engineer $engineerEmail a high priority notification $pagerEvent"
        )
        Thread.sleep(1000)

        engineerEmail
      }
    override def receive: Receive            = {
      case pagerEvent: PagerEvent =>
        sender() ! processEvent(pagerEvent)
    }
  }

  import akka.pattern.ask

  import scala.concurrent.duration._
  implicit val timeout               = Timeout(2 seconds)
  val pagerActor                     = actorSystem.actorOf(Props[PagerActor](), "pagerActor")
  val alternativePagedEngineerEmails =
    infraEvents.mapAsync(parallelism = 4)(event => (pagerActor ? event).mapTo[String])

  alternativePagedEngineerEmails.to(pagedEmailsSink).run()

  // Do not confuse mapAsync with async (Async boundary)

}
