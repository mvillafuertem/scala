package io.github.mvillafuertem.aws.cloudwatch

import java.net.URI

import io.github.mvillafuertem.aws.cloudwatch.CloudWatchApplicationIT.CloudWatchApplicationConfigurationIT
import io.github.mvillafuertem.aws.{LocalStackConfigurationIT, RichCloudWatchAsyncClientBuilder}
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentialsProvider, StaticCredentialsProvider}
import software.amazon.awssdk.http.HttpStatusCode
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.cloudwatch.model.{ListMetricsResponse, PutDashboardRequest, PutDashboardResponse}

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.Future

final class CloudWatchApplicationIT extends CloudWatchApplicationConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Put Dashboard Request" in {

    val putDashboardRequest = PutDashboardRequest
      .builder()
      .dashboardBody("{}")
      .dashboardName("")
      .build()

    val putDashboardResponse: Future[PutDashboardResponse] = cloudWatchAsyncClientDefault.putDashboard(putDashboardRequest).toScala

    putDashboardResponse.map(_.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK)

  }

  it should "List Metrics Request" in {

    val listMetricsResponse: Future[ListMetricsResponse] = cloudWatchAsyncClientDefault.listMetrics().toScala

    listMetricsResponse.map(_.sdkHttpResponse().statusCode() shouldBe HttpStatusCode.OK)

  }

  override var container: containers.DockerComposeContainer[_] = _

  override protected def beforeAll(): Unit = {
    container = dockerInfrastructure(Wait.forLogMessage(".*Starting mock CloudWatch service.*\\n", 1))
    container.start()
  }

  override protected def afterAll(): Unit = container.stop()

}

object CloudWatchApplicationIT {

  trait CloudWatchApplicationConfigurationIT extends LocalStackConfigurationIT {

    val cloudWatchAsyncClientDefault: CloudWatchAsyncClient = cloudWatchAsyncClient(
      region = Some(Region.US_EAST_1),
      endpoint = Some(URI.create(AWS_LOCALSTACK_ENDPOINT)),
      credentialsProvider = Some(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            "accessKey",
            "secretKey"
          )
        )
      )
    )

    def cloudWatchAsyncClient(
      region: Option[Region] = None,
      endpoint: Option[URI] = None,
      credentialsProvider: Option[AwsCredentialsProvider] = None
    ): CloudWatchAsyncClient =
      CloudWatchAsyncClient
        .builder()
        .add(region, _.region)
        .add(endpoint, _.endpointOverride)
        .add(credentialsProvider, _.credentialsProvider)
        .build()

  }

}
