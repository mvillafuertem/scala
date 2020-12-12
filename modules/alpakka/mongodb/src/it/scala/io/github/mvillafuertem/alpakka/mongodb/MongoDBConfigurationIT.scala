package io.github.mvillafuertem.alpakka.mongodb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import com.mongodb.reactivestreams.client.{ MongoClient, MongoClients }
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

trait MongoDBConfigurationIT {

  implicit val actorSystem: ActorSystem             = ActorSystem("MongoDBIT")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  val mongoDBConfigurationProperties: MongoDBConfigurationProperties = MongoDBConfigurationProperties()

  private val connectionString = {
    val user     = mongoDBConfigurationProperties.user
    val password = mongoDBConfigurationProperties.password
    val hostname = mongoDBConfigurationProperties.hostname
    val port     = mongoDBConfigurationProperties.port
    s"mongodb://$user:$password@$hostname:$port"
  }

  val client: MongoClient = MongoClients.create(connectionString)

  var container: containers.DockerComposeContainer[_]

  def dockerInfrastructure: containers.DockerComposeContainer[_] =
    DockerComposeContainer(
      new File(s"modules/alpakka/mongodb/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("mongo", mongoDBConfigurationProperties.port, 1, Wait.forLogMessage(".*waiting for connections on port .*\\n", 1))),
      identifier = "docker_infrastructure"
    ).container

}
