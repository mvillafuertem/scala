package io.github.mvillafuertem.akka.todo.application

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import io.github.mvillafuertem.akka.todo.domain.ToDo
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoFSM
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoFSM.{GetToDo, Opened, State, Uninitialized}
import org.scalatest.FlatSpecLike

/**
 * @author Miguel Villafuerte
 */
class ToDoApplicationSpec extends ScalaTestWithActorTestKit with FlatSpecLike {

  behavior of "ToDoApplicationSpec"

  it should "apply" in {
    val toDo = ToDo("Hola", "", 0L)
    val fsm = spawn(ToDoFSM())
    val app = ToDoApplication(fsm)
    val probe = TestProbe[State]

    app.apply(toDo)
    fsm ! GetToDo(probe.ref)
    probe.expectMessage(Uninitialized)

  }

}
