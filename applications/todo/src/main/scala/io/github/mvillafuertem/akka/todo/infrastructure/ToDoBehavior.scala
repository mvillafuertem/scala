package io.github.mvillafuertem.akka.todo.infrastructure

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, Sink}
import io.github.mvillafuertem.akka.todo.ToDoApplicationService.actorSystem
import io.github.mvillafuertem.akka.todo.domain.ToDo
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior.Command

/**
 * @author Miguel Villafuerte
 */
final class ToDoBehavior(context: ActorContext[Command])(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {

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

//        PersistenceQuery(actorSystem).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
//          .currentEventsByPersistenceId(toDo.id.toString,0, Long.MaxValue)
//          .map(_.event)
//          .runForeach(a => context.log.info(s"PEPE ~ $a"))
        context.log.info(s"$command")
        Effect.persist(Opened(toDo))
      case Close  =>
        Effect.persist(Closed)

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

  def apply(id: String)(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer): Behavior[Command] = Behaviors.setup[Command](context => {

//    PersistenceQuery(actorSystem).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
//      .currentPersistenceIds()
//      .runForeach(a => context.log.info(s"$a"))
//    journal.currentEventsByPersistenceId(id, 0, Long.MaxValue)
//      .log("asdf")
//      .map(_.event)
//      .map {
//        case State(toDo, true) => context.log.info(s"true")
//          throw new RuntimeException("ToDo is already open")
//        case State(toDo, false) =>
//          Effect.persist(Opened(toDo)).thenStop()
//          context.log.info(s"true")
//      }.runForeach(a => context.log.info("$a"))

    context.log.info(s"ToDo behavior started $id")
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


