package io.github.mvillafuertem.akka.fsm

import akka.Done
import akka.actor.typed.ActorSystem
import io.github.mvillafuertem.akka.fsm.configuration.ToDoApplicationConfiguration

object ToDoApplicationService extends App {

  ActorSystem[Done](ToDoApplicationConfiguration(), "ToDoApplicationService")

}
