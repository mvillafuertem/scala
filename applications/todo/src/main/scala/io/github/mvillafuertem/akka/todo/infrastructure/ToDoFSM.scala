package io.github.mvillafuertem.akka.todo.infrastructure

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import io.github.mvillafuertem.akka.todo.domain.ToDo

/**
 * @author Miguel Villafuerte
 */
final class ToDoFSM {

  import ToDoFSM._

  val initialState: Behavior[Command] = idle(Uninitialized)

  private def idle(state: State): Behavior[Command] = Behaviors.receiveMessage[Command] { command: Command =>
    (state, command) match {

      case (Uninitialized, Open(toDo)) =>
        idle(Opened(toDo))

      case (Opened(toDo), Close) =>
        idle(Closed(toDo))

      case (state, GetToDo(replyTo)) =>
        replyTo ! state
        Behaviors.unhandled

      case _ =>
        Behaviors.unhandled

    }
  }
}

object ToDoFSM {

  def apply(): ToDoFSM = new ToDoFSM()

  // S T A T E
  sealed trait State
  final case class Opened(toDo: ToDo) extends State
  final case class Closed(toDo: ToDo) extends State
  case object Uninitialized extends State

  // C O M M A N D
  sealed trait Command
  final case class Open(toDo: ToDo) extends Command
  final case object Close extends Command
  final case class GetToDo(replyTo: ActorRef[State]) extends Command

}
