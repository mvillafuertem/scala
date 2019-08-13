package io.github.mvillafuertem.slick.withtrait.configuration

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
 * @author Miguel Villafuerte
 */
trait InfrastructureConfigurationSpec {

  val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("withtrait.infrastructure.h2")

  val relationalRepositories = InfrastructureConfiguration.relationalRepositories(databaseConfig)

}
