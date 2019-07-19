package io.github.mvillafuertem.slick.stream.configuration

import io.github.mvillafuertem.slick.stream.repository.RelationalUserRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object UserConfiguration {

  val config: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("infrastructure.h2")

  val userRepository = new RelationalUserRepository(config)

}
