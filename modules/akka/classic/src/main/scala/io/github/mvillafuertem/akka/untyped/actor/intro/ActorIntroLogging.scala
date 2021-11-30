package io.github.mvillafuertem.akka.untyped.actor.intro

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.event.Logging

object ActorIntroLogging extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    val log = Logging(context.system, this)

    override def receive: Receive = { case message =>
      log.info(message.toString)
    }
  }

  val system                        = ActorSystem("ActorLogging")
  val simpleActorWithExplicitLogger = system.actorOf(Props[SimpleActorWithExplicitLogger]())
  simpleActorWithExplicitLogger ! "Logging a simple message"

  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b)  => log.info("Two things {} and {}", a, b)
      case message => log.info(message.toString)
    }
  }

  val actorWithLogging = system.actorOf(Props[ActorWithLogging]())
  actorWithLogging ! "Logging a simple message by extending a trait"

  actorWithLogging ! ((42, 65))

}
