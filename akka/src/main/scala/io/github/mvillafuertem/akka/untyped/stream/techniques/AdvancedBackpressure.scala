package io.github.mvillafuertem.akka.untyped.stream.techniques

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import scala.concurrent.duration._

object AdvancedBackpressure extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("AdvancedBackpressure")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // Control Backpressure
  val controlledFlow = Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, numberInstances: Int = 1)
  case class Notification(email: String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("Service discovery failed", new Date),
    PagerEvent("Illegal elements in the data pipeline", new Date),
    PagerEvent("Number of HTTP 500 spiked", new Date),
    PagerEvent("A service stopped responding", new Date)
  )

  val eventSource = Source(events)

  // A fast service for fetching oncall
  val oncallEngineer = "miguel@email.com"

  def sendEmail(notification: Notification) =
    println(s"Dear ${notification.email}, you have an event ${notification.pagerEvent}")

  val notificationSink = Flow[PagerEvent].map(event => Notification(oncallEngineer, event))
  .to(Sink.foreach[Notification](sendEmail))

  // Standard
  // eventSource.to(notificationSink).run()

  // Un-brackpressurable source
  def sendEmailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s"Dear ${notification.email}, you have an event ${notification.pagerEvent}")
  }

  val aggregateNotificationFlow = Flow[PagerEvent]
    .conflate((event1, event2) => {
      val numberInstances = event1.numberInstances + event2.numberInstances
      PagerEvent(s"You have $numberInstances events that require your attention", new Date, numberInstances)
    })
    .map(resultingEvent => Notification(oncallEngineer, resultingEvent))

  // Alternative to backpressure
  // eventSource.via(aggregateNotificationFlow).async.to(Sink.foreach[Notification](sendEmailSlow)).run()


  // Slow producers: extrapolate/expand
  val slowCounter = Source(Stream.from(1)).throttle(1, 1 second)
  val hungrySink = Sink.foreach[Int](println)

  val extrapolator = Flow[Int].extrapolate(element => Iterator.from(element))
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))

  //slowCounter.via(extrapolator).to(hungrySink).run()
  //slowCounter.via(repeater).to(hungrySink).run()

  val expander = Flow[Int].expand(element => Iterator.from(element))
  slowCounter.via(expander).to(hungrySink).run()


}
