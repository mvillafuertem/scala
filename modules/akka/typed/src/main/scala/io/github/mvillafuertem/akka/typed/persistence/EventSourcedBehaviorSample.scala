package io.github.mvillafuertem.akka.typed.persistence

import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }

final class EventSourcedBehaviorSample {}

object EventSourcedBehaviorSample {

  sealed trait Command
  final case class Add(data: String)            extends Command
  case object Clear                             extends Command
  case class GetValue(replyTo: ActorRef[State]) extends Command

  sealed trait Event
  final case class Added(data: String) extends Event
  case object Cleared                  extends Event

  final case class State(history: List[String] = Nil)

  def behavior(id: String): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = State(Nil),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case Add(data)         => Effect.persist(Added(data))
      case Clear             => Effect.persist(Cleared)
      case GetValue(replyTo) =>
        replyTo ! state
        Effect.none
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case Added(data) => state.copy((data :: state.history).take(5))
      case Cleared     => State(Nil)
    }
  }

}
