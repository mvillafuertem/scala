package io.github.mvillafuertem.akka.typed.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

final class AsynchronousBehavior {}

object AsynchronousBehavior {

  case class Ping(message: String, response: ActorRef[Pong])
  case class Pong(message: String)

  val echoActor: Behavior[Ping] = Behaviors.receive { (_, message) =>
    message match {
      case Ping(m, replyTo) =>
        replyTo ! Pong(m)
        Behaviors.same
    }
  }
}
