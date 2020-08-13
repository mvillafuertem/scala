package io.github.mvillafuertem.aws.lambda

import java.io.File
import java.net.URI

import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import io.github.mvillafuertem.aws.RichLambdaAsyncClientBuilder
import io.github.mvillafuertem.aws.lambda.LambdaApplicationIT.LambdaApplicationConfigurationIT
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaAsyncClient

import scala.concurrent.Future

final class LambdaApplicationIT extends AsyncFlatSpecLike with Matchers with BeforeAndAfterAll with LambdaApplicationConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Lambda" in {
    Future(true).map(_ shouldBe true)
  }

}

object LambdaApplicationIT {

  trait LambdaApplicationConfigurationIT {

    private val LAMBDA_PORT: Int    = 4566
    private val LAMBDA_HOST: String = "http://localhost"
    val LAMBDA_ENDPOINT: String     = s"$LAMBDA_HOST:$LAMBDA_PORT"
    val BUCKET_NAME: String         = "bucket-test"
    val KEY                         = "logback-test.xml"

    val dockerInfrastructure: containers.DockerComposeContainer[_] =
      DockerComposeContainer(
        new File(s"${System.getProperty("user.dir")}/modules/aws/src/it/resources/docker-compose.it.yml"),
        exposedServices = Seq(ExposedService("localstack", LAMBDA_PORT, 1, Wait.forLogMessage(".*Starting mock Lambda service.*\\n", 1))),
        identifier = "docker_infrastructure"
      ).container

    val lambdaAsyncClientDefault: LambdaAsyncClient = lambdaAsyncClient(
      region = Some(Region.US_EAST_1),
      endpoint = Some(URI.create(LAMBDA_ENDPOINT)),
      credentialsProvider = Some(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            "accessKey",
            "secretKey"
          )
        )
      )
    )

    def lambdaAsyncClient(
      region: Option[Region] = None,
      endpoint: Option[URI] = None,
      credentialsProvider: Option[AwsCredentialsProvider] = None
    ): LambdaAsyncClient =
      LambdaAsyncClient
        .builder()
        .add(region, _.region)
        .add(endpoint, _.endpointOverride)
        .add(credentialsProvider, _.credentialsProvider)
        .build()

  }

}
