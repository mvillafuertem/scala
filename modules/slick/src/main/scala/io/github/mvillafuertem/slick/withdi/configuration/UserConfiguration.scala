package io.github.mvillafuertem.slick.withdi.configuration

import io.github.mvillafuertem.slick.withdi.repository.RelationalUserRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object UserConfiguration {

  val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("withdi.infrastructure.h2")

  def userRepository(databaseConfig: DatabaseConfig[JdbcProfile]): RelationalUserRepository = new RelationalUserRepository(databaseConfig)

}
