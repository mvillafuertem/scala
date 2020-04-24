package io.github.mvillafuertem.akka.fsm.configuration

import com.typesafe.config.ConfigFactory
import ToDoConfigurationProperties._

/**
 * @author Miguel Villafuerte
 */
final case class ToDoConfigurationProperties(
  name: String = ConfigFactory.load().getString(s"$basePath.name"),
  port: Int = ConfigFactory.load().getInt(s"$basePath.port"),
  interface: String = ConfigFactory.load().getString(s"$basePath.interface")
)

object ToDoConfigurationProperties {

  private val basePath = "application"

}
