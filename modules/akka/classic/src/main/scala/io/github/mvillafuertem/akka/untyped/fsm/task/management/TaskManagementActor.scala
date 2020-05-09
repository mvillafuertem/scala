package io.github.mvillafuertem.akka.untyped.fsm.task.management

import akka.actor.{ Actor, ActorLogging }
import io.github.mvillafuertem.akka.untyped.fsm.task.management.TaskManagementActor._

import scala.collection.mutable
import scala.concurrent.ExecutionContext

final class TaskManagementActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.dispatcher

  def closed(task: Task): Receive = {
    case _ => sender() ! TaskManagementError(s"This task ${task.id} can not be opened, please create a new task")
  }

  def opened(task: Task): Receive = {
    case Close(id) =>
      val task = taskRepository(id)
      sender() ! TaskManagementInfo("Task closed")
      context.become(closed(task))
    case _         => sender() ! TaskManagementInfo("Task is already open")
  }

  def idle: Receive = {

    case Open(task) =>
      if (taskRepository.contains(task.id))
        sender() ! TaskManagementError("Task duplicated")
      else {
        taskRepository += (task.id -> task)
        sender() ! TaskManagementInfo("Task opened")
        context.become(opened(task))
      }

    case _ => sender() ! TaskManagementError("Task not exits")

  }

  override def receive: Receive = idle

}

object TaskManagementActor {

  // MODEL
  case class Task(id: Long, title: String, content: String, timestamp: Long)

  // E V E N T
  case class Open(task: Task)
  case class Close(id: Long)

  // S T A T E
  // idle
  // opened
  // closed

  // I N F O
  case class TaskManagementInfo(message: String)

  // E R R O R
  case class TaskManagementError(message: String)

  private val taskRepository = mutable.Map[Long, Task]()

}
