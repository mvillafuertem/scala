package io.github.mvillafuertem.tapir.configuration.properties

import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author
 *   Miguel Villafuerte
 */
final case class ProductsConfigurationProperties(
  name: String,
  interface: String,
  port: Int
) {
  def withName(name: String): ProductsConfigurationProperties =
    copy(name = name)

  def withInterface(interface: String): ProductsConfigurationProperties =
    copy(interface = interface)

  def withPort(port: Int): ProductsConfigurationProperties =
    copy(port = port)
}

object ProductsConfigurationProperties {

  type ZProductsConfigurationProperties = ProductsConfigurationProperties

  def apply(config: Config = ConfigFactory.load().getConfig("application")): ProductsConfigurationProperties =
    new ProductsConfigurationProperties(
      name = config.getString("name"),
      interface = config.getString("server.interface"),
      port = config.getInt("server.port")
    )

}
