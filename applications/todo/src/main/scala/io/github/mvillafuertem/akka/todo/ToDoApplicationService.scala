package io.github.mvillafuertem.akka.todo

import akka.actor.typed.ActorSystem
import io.github.mvillafuertem.akka.todo.configuration.ToDoConfiguration
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior.Command

object ToDoApplicationService extends ToDoConfiguration with App {

  implicit val actorSystem: ActorSystem[Command] = ActorSystem[Command](
    ToDoBehavior("id"),
    "ToDoServiceTypedSystem")

}