package io.github.mvillafuertem.aws.cloudwatch

import io.github.mvillafuertem.aws.LocalStackConfigurationIT
import io.github.mvillafuertem.aws.cloudwatch.CloudWatchApplicationIT.CloudWatchApplicationConfigurationIT
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.Future

final class CloudWatchApplicationIT extends CloudWatchApplicationConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Create Function Request" in {

    Future(true).map(_ shouldBe true)
  }

  override var container: containers.DockerComposeContainer[_] = _

  override protected def beforeAll(): Unit = {
    container = dockerInfrastructure(Wait.forLogMessage(".*Starting mock CloudWatch Logs service.*\\n", 1))
    container.start()
  }

  override protected def afterAll(): Unit = container.stop()

}

object CloudWatchApplicationIT {

  trait CloudWatchApplicationConfigurationIT extends LocalStackConfigurationIT {}

}
