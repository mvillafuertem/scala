package io.github.mvillafuertem.tapir.configuration

import com.typesafe.config.ConfigFactory
import io.github.mvillafuertem.tapir.configuration.ProductsConfigurationProperties._

/**
 * @author Miguel Villafuerte
 */
final case class ProductsConfigurationProperties(
  name: String = ConfigFactory.load().getString(s"$path.name"),
  interface: String = ConfigFactory.load().getString(s"$path.server.interface"),
  port: Int = ConfigFactory.load().getInt(s"$path.server.port")
)

object ProductsConfigurationProperties {

  private[configuration] val path: String = "application"

}
