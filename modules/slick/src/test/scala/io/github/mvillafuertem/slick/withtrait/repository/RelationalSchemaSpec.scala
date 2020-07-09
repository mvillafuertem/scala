package io.github.mvillafuertem.slick.withtrait.repository

import io.github.mvillafuertem.slick.withtrait.configuration.InfrastructureConfigurationSpec
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * @author Miguel Villafuerte
 */
final class RelationalSchemaSpec extends InfrastructureConfigurationSpec with AsyncFlatSpecLike with Matchers {

  "Schema" should "print the database schema for copy & paste" in {

    import databaseConfig.profile.api._

    val keywords = Seq("create", "index", "table", "constraint", "foreign", "alter")
    val result   = (relationalRepositories.vertexTable.schema ++ relationalRepositories.edgeTable.schema).createStatements.mkString("\n")
    info(result) // for copy & paste
    assert(keywords.forall(kw => result.contains(kw)))
  }

}
