package io.github.mvillafuertem.todo.infrastructure

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import io.github.mvillafuertem.todo.domain.ToDo
import io.github.mvillafuertem.todo.infrastructure.ToDoFSM.Command
import io.github.mvillafuertem.todo.domain.ToDo

/**
 * @author Miguel Villafuerte
 */
final class ToDoFSM(context: ActorContext[Command]) {

  import ToDoFSM._

  def initialState: Behavior[Command] = idle(Uninitialized)

  private def idle(state: State): Behavior[Command] =

    Behaviors.receiveMessage[Command] { command: Command =>
      (state, command) match {

        case (Uninitialized, Open(toDo)) =>
          context.log.info(s"$state")
          idle(Opened(toDo))

        case (Opened(toDo), Close) =>
          context.log.info(s"$state")
          idle(Closed(toDo))

        case (state, GetToDo(replyTo)) =>
          //context.log.info(s"$state")
          replyTo ! state
          Behaviors.unhandled

        case _ =>
          context.log.info(s"$state")
          Behaviors.unhandled

      }

    }
}

object ToDoFSM {

  def apply():Behavior[Command] =
    Behaviors.setup[Command](context => new ToDoFSM(context).initialState)

  // S T A T E
  sealed trait State

  // C O M M A N D
  sealed trait Command

  final case class Opened(toDo: ToDo) extends State

  final case class Closed(toDo: ToDo) extends State

  final case class Open(toDo: ToDo) extends Command

  final case class GetToDo(replyTo: ActorRef[State]) extends Command

  case object Uninitialized extends State

  final case object Close extends Command

}
