package io.github.mvillafuertem.akka.fsm.application

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import io.github.mvillafuertem.akka.fsm.domain.ToDo
import io.github.mvillafuertem.akka.fsm.infrastructure.ToDoFSM
import io.github.mvillafuertem.akka.fsm.infrastructure.ToDoFSM.{ GetToDo, State, Uninitialized }
import org.scalatest.flatspec.AnyFlatSpecLike

/**
 * @author
 *   Miguel Villafuerte
 */
class ToDoApplicationSpec extends ScalaTestWithActorTestKit with AnyFlatSpecLike {

  behavior of "ToDoApplicationSpec"

  it should "apply" in {
    val toDo  = ToDo("Hola", "", 0L)
    val fsm   = spawn(ToDoFSM())
    val app   = ToDoApplication(fsm)
    val probe = TestProbe[State]()

    app.apply(toDo)
    fsm ! GetToDo(probe.ref)
    probe.expectMessage(Uninitialized)

  }

}
