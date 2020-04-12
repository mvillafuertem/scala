package io.github.mvillafuertem.akka.typed.persistence.task.manager

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._


object TaskManager extends App {

  sealed trait Command
  final case class StartTask(taskId: String) extends Command
  final case class NextStep(taskId: String, instruction: String) extends Command
  final case class EndTask(taskId: String) extends Command

  sealed trait Event
  final case class TaskStarted(taskId: String) extends Event
  final case class TaskStep(taskId: String, instruction: String) extends Event
  final case class TaskCompleted(taskId: String) extends Event

  final case class State(taskIdInProgress: Option[String])


  def apply(persistenceId: PersistenceId): Behavior[Command] = Behaviors.setup {

    context => context.setLoggerName(this.getClass)

      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = State(None),
        commandHandler = (state, command) => onCommand(context, state, command),
        eventHandler = (state, event) => applyEvent(context, state, event))
        .onPersistFailure(SupervisorStrategy.restartWithBackoff(1.second, 30.seconds, 0.2))
  }

  private def onCommand(context: ActorContext[Command], state: State, command: Command): Effect[Event, State] = {
    state.taskIdInProgress match {
      case None =>
        command match {
          case StartTask(taskId) =>
            context.log.info(s"1. $taskId")
            Effect.persist(TaskStarted(taskId))
          case _ =>
            Effect.unhandled
        }

      case Some(inProgress) =>
        command match {
          case StartTask(taskId) =>
            if (inProgress == taskId)
              Effect.none // duplicate, already in progress
            else
            // other task in progress, wait with new task until later
              Effect.stash()

          case NextStep(taskId, instruction) =>
            if (inProgress == taskId)
              Effect.persist(TaskStep(taskId, instruction))
            else
            // other task in progress, wait with new task until later
              Effect.stash()

          case EndTask(taskId) =>
            if (inProgress == taskId)
              Effect.persist(TaskCompleted(taskId)).thenUnstashAll() // continue with next task
            else
            // other task in progress, wait with new task until later
              Effect.stash()
        }
    }
  }

  private def applyEvent(context: ActorContext[Command], state: State, event: Event): State = {
    event match {
      case TaskStarted(taskId) =>
        context.log.info(s"2. $taskId")
        State(Option(taskId))
      case TaskStep(_, _)      =>
        context.log.info(s"3. $state")
        state
      case TaskCompleted(_)    => State(None)
    }
  }


  val typedSystem = ActorSystem(apply(PersistenceId.ofUniqueId("taskManager")), "TaskManagerTypedSystem", ConfigFactory.load().getConfig("postgres"))

  typedSystem ! StartTask("Task con ID pepe")
  typedSystem ! NextStep("Task con ID pepe", "Pending")



}
