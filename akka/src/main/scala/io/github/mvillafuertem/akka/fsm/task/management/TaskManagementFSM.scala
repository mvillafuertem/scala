package io.github.mvillafuertem.akka.fsm.task.management

import akka.actor.FSM
import io.github.mvillafuertem.akka.fsm.task.management.TaskManagementFSM._

import scala.collection.mutable

final class TaskManagementFSM extends FSM[TaskState, TaskData] {

  startWith(Idle, NotExist)

  when(Idle) {
    case Event(Open(task), NotExist) =>
      if(taskRepository.contains(task.id)) {
        sender() ! TaskManagementError("Task duplicated")
        stay()
      } else {
        taskRepository += (task.id -> task)
        sender() ! TaskManagementInfo("Task opened")
        //context.become(opened(task))
        goto(Opened) using Open(task)
      }
    case _ => sender() ! TaskManagementError("Task not exits")
      stay()
  }

  when(Opened) {
    case Event(Close(id), Open(task)) =>
      val task = taskRepository(id)
      sender() ! TaskManagementInfo("Task closed")
      //context.become(closed(task))
      goto(Closed) using Close(id)
    case _ =>
      sender() ! TaskManagementInfo("Task is already open")
      stay()
  }

  when(Closed) {
    case Event(_, Close(id)) =>
      sender() ! TaskManagementError(s"This task $id can not be opened, please create a new task")
      stay()
  }


  initialize()

}

object TaskManagementFSM {

  // MODEL
  case class Task(id: Long, title: String, content: String, timestamp: Long)

  // E V E N T
  sealed trait TaskData
  case object NotExist extends TaskData
  case class Open(task: Task) extends TaskData
  case class Close(id: Long) extends TaskData

  // S T A T E
  sealed trait TaskState
  case object Idle extends TaskState
  case object Opened extends TaskState
  case object Closed extends TaskState

  // I N F O
  case class TaskManagementInfo(message: String)

  // E R R O R
  case class TaskManagementError(message: String)

  private val taskRepository = mutable.Map[Long, Task]()

}
