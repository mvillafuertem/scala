package io.github.mvillafuertem.alpakka.sqs

import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.scaladsl.{ SqsPublishFlow, SqsSource }
import akka.stream.alpakka.sqs.{ SqsPublishSettings, SqsSourceSettings }
import akka.stream.scaladsl.{ Sink, Source }
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import io.github.mvillafuertem.alpakka.sqs.SqsSourceIT.SqsSourceConfigurationIT
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.testcontainers.containers
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{ Message, ReceiveMessageRequest, ReceiveMessageResponse, SendMessageRequest }

import java.net.URI
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._

final class SqsSourceIT extends SqsSourceConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  ignore should "receive message response using sqsClient" in {

    val result: Future[ReceiveMessageResponse] =
      awsSqsClient
        .receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build())
        .asScala

    val actual: mutable.Seq[Message] = result.futureValue.messages().asScala
    actual.size shouldBe 1
    actual.head.body() shouldBe """{"id":1,"name":"Test"}"""

  }

  it should "receive message response using source" in {

    val source = SqsSource(
      queueUrl,
      SqsSourceSettings().withCloseOnEmptyReceive(true)
    )(awsSqsClient)

    val result: Future[Message] = source.runWith(Sink.head)

    result.futureValue.body() shouldBe """{"id":1,"name":"Test"}"""

  }

}

object SqsSourceIT {

  trait SqsSourceConfigurationIT
      extends SqsConfigurationIT
      with AnyFlatSpecLike
      with ScalaFutures
      with BeforeAndAfterEach
      with BeforeAndAfterAll
      with Matchers {

    implicit val system: ActorSystem = ActorSystem()

    implicit val defaultPatience: PatienceConfig =
      PatienceConfig(timeout = 30.seconds, interval = 200.millis)

    private val uri                                            = "http://localhost:9324"
    private val credentialsProvider: StaticCredentialsProvider = StaticCredentialsProvider
      .create(AwsBasicCredentials.create("x", "x"))

    protected val queueUrl = s"$uri/queue/queue1"

    implicit val awsSqsClient: SqsAsyncClient = SqsAsyncClient
      .builder()
      .credentialsProvider(
        credentialsProvider
      )
      .endpointOverride(URI.create(uri))
      .region(Region.of("elasticmq"))
      .httpClient(AkkaHttpClient.builder().withActorSystem(system).build())
//      .overrideConfiguration(
//        ClientOverrideConfiguration
//          .builder()
//          .retryPolicy(
//            // This example shows the AWS SDK 2 `RetryPolicy.defaultRetryPolicy()`
//            // See https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/core/retry/RetryPolicy.html
//            RetryPolicy.builder
//              .backoffStrategy(BackoffStrategy.defaultStrategy)
//              .throttlingBackoffStrategy(BackoffStrategy.defaultThrottlingStrategy)
//              .numRetries(SdkDefaultRetrySetting.DEFAULT_MAX_RETRIES)
//              .retryCondition(RetryCondition.defaultRetryCondition)
//              .build
//          )
//          .build()
//      )
      .build()

    override protected def beforeEach(): Unit =
      Source
        .single(s"{\"id\":1,\"name\":\"Test\"}")
        .map(SendMessageRequest.builder().messageBody(_).build())
        .via(SqsPublishFlow(queueUrl, SqsPublishSettings.create())(awsSqsClient))
        .runWith(Sink.foreach(println))

    override protected def afterAll(): Unit = {
      system.terminate.futureValue
      awsSqsClient.close()
      container.stop()
    }
    override var container: containers.DockerComposeContainer[_] = _

    override protected def beforeAll(): Unit = {
      container = dockerInfrastructure
      container.start()
    }
  }

}
