package io.github.mvillafuertem.zio.s3.configuration

import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region

import java.io.File

trait LocalstackConfiguration {

  private val port: Int                                        = 4570
  protected val uri: String                                    = s"http://localhost:$port"
  protected val accessKey: String                              = "accessKeyId"
  protected val secretAccessKey: String                        = "secretAccessKey"
  protected val defaultRegion: Region                          = Region.US_EAST_1
  protected val credentialsProvider: StaticCredentialsProvider = StaticCredentialsProvider
    .create(AwsBasicCredentials.create(accessKey, secretAccessKey))

  lazy val container: DockerComposeContainer =
    DockerComposeContainer(
      new File("modules/zio/s3/src/test/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("localstack", port, 1, Wait.forLogMessage(".*Ready.*", 1))),
      identifier = "docker_infrastructure"
    )

}
