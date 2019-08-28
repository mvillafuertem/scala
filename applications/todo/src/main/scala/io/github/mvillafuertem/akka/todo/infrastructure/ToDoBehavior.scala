package io.github.mvillafuertem.akka.todo.infrastructure

import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.mvillafuertem.akka.todo.domain.ToDo
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior.Command

/**
 * @author Miguel Villafuerte
 */
final class ToDoBehavior(context: ActorContext[Command]) {

  import ToDoBehavior._

  def behavior(id: String): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId(id),
      emptyState = State(ToDo("", "", 0L), false),
      commandHandler = commandHandler,
      eventHandler = eventHandler)

  val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case Open(toDo) =>
        if (state.opened) {
          throw new RuntimeException("ToDo is already open")
          Effect.none
        } else {
          Effect.persist(Opened(toDo))
        }
      case Close  => Effect.persist(Closed)
      case GetToDo(replyTo) =>
        replyTo ! state
        Effect.none
    }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case Opened(toDo) => State(toDo, true)
      case Closed => State(state.toDo, false)
    }
  }


}

object ToDoBehavior {

  def apply(id: String): Behavior[Command] = Behaviors.setup[Command](context => {
    context.log.info("ToDo behavior started")
    new ToDoBehavior(context).behavior(id)
  })

  // S T A T E
  final case class State(toDo: ToDo, opened: Boolean)

  // C O M M A N D
  sealed trait Command
  final case class Open(toDo: ToDo) extends Command
  final case object Close extends Command
  final case class GetToDo(replyTo: ActorRef[State]) extends Command

  // E V E N T
  sealed trait Event
  final case class Opened(toDo: ToDo) extends Event
  final case object Closed extends Event

}


