package io.github.mvillafuertem.todo

import akka.Done
import akka.actor.typed.ActorSystem
import io.github.mvillafuertem.todo.configuration.ToDoApplicationConfiguration

object ToDoApplicationService extends App {

  ActorSystem[Done](ToDoApplicationConfiguration(), "ToDoApplicationService")

}