package io.github.mvillafuertem.slick.withdi.configuration

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait UserConfigurationSpec {

  val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("withdi.infrastructure.h2")

  val userRepository = UserConfiguration.userRepository(databaseConfig)

}
