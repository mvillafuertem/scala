package io.github.mvillafuertem.alpakka.sqs

import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.scaladsl.{ SqsAckFlow, SqsPublishFlow, SqsSource }
import akka.stream.alpakka.sqs.{ MessageAction, SqsAckSettings, SqsPublishSettings, SqsSourceSettings }
import akka.stream.scaladsl.{ Sink, Source }
import akka.{ Done, NotUsed }
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
import software.amazon.awssdk.services.sqs.model.{ Message, SendMessageRequest }

import java.net.URI
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

final class SqsSourceIT extends SqsSourceConfigurationIT {

  it should "receive message response" in {

    // g i v e n
    val source: Source[Message, NotUsed] = SqsSource(
      queue1Url,
      SqsSourceSettings().withCloseOnEmptyReceive(true)
    )(awsSqsClient)

    // w h e n
    val actual: Future[Seq[String]] = source
      .wireTap(println(_))
      .map(MessageAction.delete)
      .via(SqsAckFlow(queue1Url, SqsAckSettings.create)(awsSqsClient))
      .wireTap(println(_))
      .runWith(Sink.seq)
      .map(_.map(_.messageAction.message.body()))(system.dispatcher)

    // t h e n
    actual.futureValue shouldBe Seq("""{"id":1,"name":"Test"}""")

  }

  it should "receive ordered message response using fifo queue" in {

    // g i v e n
    val source: Source[Message, NotUsed] = SqsSource(
      queue2Url,
      SqsSourceSettings().withCloseOnEmptyReceive(true)
    )(awsSqsClient)

    // w h e n
    val actual: Future[Seq[String]] = source
      .wireTap(println(_))
      .map(MessageAction.delete)
      .via(SqsAckFlow(queue2Url, SqsAckSettings.create)(awsSqsClient))
      .wireTap(println(_))
      .runWith(Sink.seq)
      .map(_.map(_.messageAction.message.body()))(system.dispatcher)

    // t h e n
    actual.futureValue shouldBe (1 to 10).map(n => s"""{"id":$n,"name":"Test"}""")

  }

  it should "receive message response and send enriched message to another queue" in {

    // g i v e n
    val source: Source[Message, NotUsed] = SqsSource(
      queue3Url,
      SqsSourceSettings().withCloseOnEmptyReceive(true)
    )(awsSqsClient)

    // w h e n
    import io.circe._
    val actual: Future[Seq[String]] = source
      .wireTap(println(_))
      .mapAsync(1)(msg =>
        Source
          .single[Message](msg)
          .map(msg =>
            parser
              .parse(msg.body())
              .map(json => json.deepMerge(Json.obj(("surname", Json.fromString("Integration")))))
              .getOrElse(Json.Null)
              .noSpaces
          )
          .map(SendMessageRequest.builder().messageBody(_).build())
          .wireTap(println(_))
          .via(SqsPublishFlow(queue1Url)(awsSqsClient))
          .runWith(Sink.foreach(println))
          .map(_ => msg)(system.dispatcher)
      )
      .map(MessageAction.delete)
      .via(SqsAckFlow(queue3Url, SqsAckSettings.create)(awsSqsClient))
      .wireTap(println(_))
      .runWith(Sink.seq)
      .map(_.map(_.messageAction.message.body()))(system.dispatcher)

    // t h e n
    (actual.futureValue should contain).theSameElementsAs((1 to 10).map(n => s"""{"id":$n,"name":"Test"}"""))

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

    protected val queue1Url = s"$uri/queue/queue1"
    protected val queue2Url = s"$uri/queue/queue2.fifo"
    protected val queue3Url = s"$uri/queue/queue3"

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

    override protected def afterAll(): Unit                      = {
      system.terminate.futureValue
      awsSqsClient.close()
      container.stop()
    }
    override var container: containers.DockerComposeContainer[_] = _

    override protected def beforeAll(): Unit = {
      container = dockerInfrastructure
      container.start()
      Source
        .single("{\"id\":1,\"name\":\"Test\"}")
        .map(SendMessageRequest.builder().messageBody(_).build())
        .via(SqsPublishFlow(queue1Url, SqsPublishSettings.create())(awsSqsClient))
        .runWith(Sink.foreach(println))
        .futureValue shouldBe Done

      Source
        .fromIterator(() => (1 to 10).iterator)
        .map(n => s"""{\"id\":$n,\"name\":\"Test\"}""")
        .map(SendMessageRequest.builder().messageBody(_).messageGroupId("foo").build())
        .via(SqsPublishFlow(queue2Url, SqsPublishSettings.create())(awsSqsClient))
        .runWith(Sink.foreach(println))
        .futureValue shouldBe Done

      Source
        .fromIterator(() => (1 to 10).iterator)
        .map(n => s"""{\"id\":$n,\"name\":\"Test\"}""")
        .map(SendMessageRequest.builder().messageBody(_).build())
        .via(SqsPublishFlow(queue3Url, SqsPublishSettings.create())(awsSqsClient))
        .runWith(Sink.foreach(println))
        .futureValue shouldBe Done
    }
  }

}
