package io.github.mvillafuertem.alpakka.sqs

import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

trait SqsConfigurationIT {

  var container: containers.DockerComposeContainer[_]

  def dockerInfrastructure: containers.DockerComposeContainer[_] =
    DockerComposeContainer(
      new File(s"modules/alpakka/sqs/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("elasticmq", 9324, 1, Wait.forLogMessage(".*Creating queue QueueData.*", 2))),
      identifier = "docker_infrastructure"
    ).container

}
