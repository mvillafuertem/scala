package io.github.mvillafuertem.akka.typed.persistence

import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

final class AlertBehavior {

}

object AlertBehavior {

  // D O M A I N
  case class Alert(id: Long, title: String, content: String, timestamp: Long, open: Boolean)

  final case class State(history: List[Alert] = Nil)


  // C O M M A N D
  sealed trait Command
  final case class Open(alert: Alert) extends Command
  final case class Close(id: Long) extends Command
  final case class GetAlert(replyTo: ActorRef[State]) extends Command

  // E V E N T
  sealed trait Event
  final case class Opened(alert: Alert) extends Event
  final case class Closed(id: Long) extends Event

  // B E H A V I O R
  def behavior(id: String): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = State(Nil),
      commandHandler = commandHandler,
      eventHandler = eventHandler)

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case Open(alert) =>
        if (state.history.exists(_.id == alert.id)) {
          throw new RuntimeException("Alert Duplicated")
          Effect.none
        } else {
          Effect.persist(Opened(alert))
        }
      case Close(id)     => Effect.persist(Closed(id))
      case GetAlert(replyTo) =>
        replyTo ! state
        Effect.none
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case Opened(alert) => state.copy((alert :: state.history).take(5))
      case Closed(id)     => state.copy(state.history.dropWhile(_.id == id))
    }
  }


}
