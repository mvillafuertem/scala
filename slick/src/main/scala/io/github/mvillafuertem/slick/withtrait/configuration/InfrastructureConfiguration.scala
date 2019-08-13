package io.github.mvillafuertem.slick.withtrait.configuration

import io.github.mvillafuertem.slick.withtrait.repository.{Profile, RelationalRepositories}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object InfrastructureConfiguration {

  lazy val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("infrastructure.postgres")

  def relationalRepositories(databaseConfig: DatabaseConfig[JdbcProfile]) =
    new RelationalRepositories with Profile {

      override val profile = databaseConfig.profile

      val edgeRepository = new RelationalEdgeRepository(databaseConfig.db)

      val vertexRepository = new RelationalVertexRepository(databaseConfig.db)

    }

}
