package io.github.mvillafuertem.akka.fsm.infrastructure

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import io.github.mvillafuertem.akka.fsm.domain.ToDo
import org.scalatest.flatspec.AnyFlatSpecLike

/**
 * @author Miguel Villafuerte
 */
final class ToDoFSMSpec extends ScalaTestWithActorTestKit with AnyFlatSpecLike {

  import ToDoFSM._

  behavior of "FSMDocSpec"

  it should "FSMDocSpec" in {

    val toDo    = ToDo("Hola", "que tal", 0L)
    val buncher = spawn(ToDoFSM())
    val probe   = TestProbe[State]()

    buncher ! GetToDo(probe.ref)
    probe.expectMessage(Uninitialized)

    buncher ! Open(toDo)
    buncher ! GetToDo(probe.ref)
    probe.expectMessage(Opened(toDo))

    buncher ! Close
    buncher ! GetToDo(probe.ref)
    probe.expectMessage(Closed(toDo))

    buncher ! Open(toDo)
    buncher ! GetToDo(probe.ref)
    probe.expectMessage(Closed(toDo))

  }
}
