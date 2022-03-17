package io.github.mvillafuertem.tapir.configuration.properties

import com.typesafe.config.ConfigFactory
import zio.Layer
import zio.config.ConfigDescriptor.{ int, nested, string }
import zio.config.{ ReadError, _ }
import zio.config.typesafe._

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

  private val configDescriptor: ConfigDescriptor[ProductsConfigurationProperties] =
    string("name")
      .zip(nested("server")(string("interface")))
      .zip(nested("server")(int("port")))
      .to[ProductsConfigurationProperties]

  val live: Layer[ReadError[String], ZProductsConfigurationProperties] =
    TypesafeConfig.fromTypesafeConfig(ConfigFactory.load().getConfig("application"), configDescriptor)
}
