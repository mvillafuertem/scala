package io.github.mvillafuertem.akka.fsm.configuration

import io.github.mvillafuertem.akka.fsm.api.ToDoAPI

/**
 * @author
 *   Miguel Villafuerte
 */
trait ToDoConfiguration {

  // val log: Logger

  lazy val toDoConfigurationProperties = ToDoConfigurationProperties()
//  lazy val toDoAPI: ToDoAPI = ToDoAPI(log)
  lazy val toDoAPI: ToDoAPI            = ToDoAPI()

}
