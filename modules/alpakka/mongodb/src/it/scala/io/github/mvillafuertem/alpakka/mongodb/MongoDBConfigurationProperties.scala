package io.github.mvillafuertem.alpakka.mongodb

import com.typesafe.config.{ Config, ConfigFactory }

final case class MongoDBConfigurationProperties(user: String, password: String, hostname: String, port: Int) {
  
  def withUser(user: String): MongoDBConfigurationProperties =
    copy(user = user)

  def withPassword(password: String): MongoDBConfigurationProperties =
    copy(password = password)
    
  def withHostname(hostname: String): MongoDBConfigurationProperties =
    copy(hostname = hostname)

  def withPort(port: Int): MongoDBConfigurationProperties =
    copy(port = port)

}

object MongoDBConfigurationProperties {

  val default: Config = ConfigFactory.load().getConfig("infrastructure.mongodb")

  def apply(config: Config = default): MongoDBConfigurationProperties =
    new MongoDBConfigurationProperties(
      user = config.getString("user"),
      password = config.getString("password"),
      hostname = config.getString("hostname"),
      port = config.getInt("port"),
    )

}
