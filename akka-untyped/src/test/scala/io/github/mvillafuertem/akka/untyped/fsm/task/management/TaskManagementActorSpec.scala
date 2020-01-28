package io.github.mvillafuertem.akka.untyped.fsm.task.management

import java.util.Date

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import io.github.mvillafuertem.akka.untyped.fsm.task.management.TaskManagementActor._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.{BeforeAndAfterAll, OneInstancePerTest}

class TaskManagementActorSpec extends TestKit(ActorSystem("TaskManagementActorSpec"))
  with ImplicitSender
  with AnyFlatSpecLike
  with BeforeAndAfterAll
  with OneInstancePerTest {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  behavior of "A task management actor"

  it should "error when not initialized" in {

    // G I V E N
    val taskManagement = system.actorOf(Props[TaskManagementActor])

    // W H E N
    taskManagement ! Close(987654321L)

    // T H E N
    expectMsg(TaskManagementError("Task not exits"))

  }

  it should "open task" in {

    // G I V E N
    val taskManagement = system.actorOf(Props[TaskManagementActor])
    val task: Task = Task(123456789L, "Title", "Content", new Date().toInstant.toEpochMilli)

    // W H E N
    taskManagement ! Open(task)

    // T H E N
    expectMsg(TaskManagementInfo("Task opened"))

  }

  it should "detect task duplicated" in {

    // G I V E N
    val taskManagement = system.actorOf(Props[TaskManagementActor])
    val task: Task = Task(987654321L, "Title", "Content", new Date().toInstant.toEpochMilli)
    taskManagement ! Open(task)
    expectMsg(TaskManagementInfo("Task opened"))

    // W H E N
    taskManagement ! Open(task)

    // T H E N
    expectMsg(TaskManagementInfo("Task is already open"))

  }

  it should "close task" in {

    // G I V E N
    val taskManagement = system.actorOf(Props[TaskManagementActor])
    val task: Task = Task(876543219L, "Title", "Content", new Date().toInstant.toEpochMilli)
    taskManagement ! Open(task)
    expectMsg(TaskManagementInfo("Task opened"))

    // W H E N
    taskManagement ! Close(876543219L)

    // T H E N
    expectMsg(TaskManagementInfo("Task closed"))

  }

  it should "error close task" in {

    // G I V E N
    val taskManagement = system.actorOf(Props[TaskManagementActor])

    // W H E N
    taskManagement ! Close(234567891L)

    // T H E N
    expectMsg(TaskManagementError("Task not exits"))

  }

}
