package io.github.mvillafuertem.aws

import java.io.File

import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy

trait LocalStackConfigurationIT extends AsyncFlatSpecLike with Matchers with BeforeAndAfterAll {

  private val AWS_LOCALSTACK_HOST: String = "http://localhost"
  private val AWS_LOCALSTACK_PORT: Int    = 4566

  val AWS_LOCALSTACK_ENDPOINT: String = s"$AWS_LOCALSTACK_HOST:$AWS_LOCALSTACK_PORT"

  var container: containers.DockerComposeContainer[_]

  def dockerInfrastructure(logMessageWaitStrategy: LogMessageWaitStrategy): containers.DockerComposeContainer[_] =
    DockerComposeContainer(
      new File(s"${System.getProperty("user.dir")}/modules/aws/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("localstack", AWS_LOCALSTACK_PORT, 1, logMessageWaitStrategy)),
      identifier = "docker_infrastructure"
    ).container

}
