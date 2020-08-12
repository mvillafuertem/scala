package io.github.mvillafuertem.aws.s3

import java.io.File
import java.util.concurrent.CompletionException

import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.services.s3.model.{ HeadObjectRequest, NoSuchKeyException }
import software.amazon.awssdk.services.s3.{ S3AsyncClient, S3AsyncClientBuilder }

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.ExecutionContext.Implicits.global

final class S3ApplicationSpec {

  val s3AsyncClient: S3AsyncClient = {
    implicit class RichBuilder(s3ClientBuilder: S3AsyncClientBuilder) {
      def add[T](value: Option[T], builder: S3AsyncClientBuilder => T => S3AsyncClientBuilder): S3AsyncClientBuilder =
        value.fold(s3ClientBuilder)(builder(s3ClientBuilder))
    }

    S3AsyncClient
      .builder()
      .add(None, _.region)
      .add(None, _.endpointOverride)
      .add(None, _.credentialsProvider)
      .build()
  }

  s3AsyncClient
    .headObject(
      HeadObjectRequest
        .builder()
        .bucket("bucket")
        .key("")
        .build()
    )
    .toScala
    .map(_ => true)
    .recover {
      case e: CompletionException if e.getCause.isInstanceOf[NoSuchKeyException] =>
        false
    }

}

object NumbersTransactionIT {

  trait NumbersTransactionConfigurationIT {

    val dockerInfrastructure: containers.DockerComposeContainer[_] = DockerComposeContainer(
      new File("docker-compose.yml"),
      exposedServices = Seq(ExposedService("kafka", 9092, 1, Wait.forLogMessage(".*started .*\\n", 1))),
      identifier = "docker_infrastructure"
    ).container

  }
}
