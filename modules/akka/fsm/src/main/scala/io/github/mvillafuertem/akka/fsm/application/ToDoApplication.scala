package io.github.mvillafuertem.akka.fsm.application

import akka.actor.typed.ActorRef
import io.github.mvillafuertem.akka.fsm.domain.ToDo
import io.github.mvillafuertem.akka.fsm.infrastructure.ToDoFSM.{ Close, Command, Open }

/**
 * @author Miguel Villafuerte
 */
final class ToDoApplication(fsm: ActorRef[Command]) {

  def apply(toDo: ToDo): Unit =
    if (toDo.content.isEmpty)
      fsm ! Close
    else
      fsm ! Open(toDo)

}

object ToDoApplication {

  def apply(fsm: ActorRef[Command]): ToDoApplication = new ToDoApplication(fsm)

}
