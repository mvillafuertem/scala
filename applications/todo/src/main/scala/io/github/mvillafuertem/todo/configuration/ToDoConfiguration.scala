package io.github.mvillafuertem.todo.configuration

import akka.actor.typed.Logger
import io.github.mvillafuertem.todo.api.ToDoAPI

/**
 * @author Miguel Villafuerte
 */
trait ToDoConfiguration {

  //val log: Logger

  lazy val toDoConfigurationProperties = ToDoConfigurationProperties()
//  lazy val toDoAPI: ToDoAPI = ToDoAPI(log)
  lazy val toDoAPI: ToDoAPI = ToDoAPI()

}
