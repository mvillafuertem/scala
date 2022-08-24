package io.github.mvillafuertem.alpakka.sns

import akka.stream.alpakka.sns.scaladsl.SnsPublisher
import akka.stream.alpakka.sqs.scaladsl.{ SqsAckFlow, SqsPublishFlow, SqsSource }
import akka.stream.alpakka.sqs.{ MessageAction, SqsAckSettings, SqsPublishSettings, SqsSourceSettings }
import akka.stream.scaladsl.{ Sink, Source }
import akka.{ Done, NotUsed }
import io.github.mvillafuertem.alpakka.sns.SqsSourceIT.SqsSourceConfigurationIT
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.testcontainers.containers
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sqs.model.{ Message, SendMessageRequest }

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContextExecutor, Future }

final class SqsSourceIT extends SqsSourceConfigurationIT {

  it should "receive message response" in {

    // g i v e n
    val source: Source[Message, NotUsed] = createSqsSource(queue1Url)

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
    val source: Source[Message, NotUsed] = createSqsSource(queue2Url)

    // w h e n
    val actual: Future[Seq[String]] = source
      .wireTap(println(_))
      .map(MessageAction.delete)
      .via(SqsAckFlow(queue2Url, SqsAckSettings.create)(awsSqsClient))
      .wireTap(println(_))
      .runWith(Sink.seq)
      .map(_.map(_.messageAction.message.body()))(system.dispatcher)

    // t h e n
    (actual.futureValue should contain).theSameElementsAs((1 to 10).map(n => s"""{"id":$n,"name":"Test"}"""))

  }

  it should "receive message response and send enriched message to a topic" in {

    // g i v e n
    val source: Source[Message, NotUsed] = createSqsSource(queue3Url)

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
          .map(PublishRequest.builder().message(_).build())
          .wireTap(println(_))
          .via(SnsPublisher.publishFlow(topicArn))
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

  it should "receive enriched message response from topic through subscribed queue" in {

    // g i v e n
    val source: Source[Message, NotUsed] = createSqsSource(queue1Url)

    // w h e n
    import io.circe.parser._
    val actual = source
      .wireTap(println(_))
      .map(MessageAction.delete)
      .via(SqsAckFlow(queue1Url, SqsAckSettings.create)(awsSqsClient))
      .runWith(Sink.seq)
      .map(
        _.map(_.messageAction.message.body()).map { jsonString =>
          (for {
            json    <- parse(jsonString)
            message <- json.hcursor.get[String]("Message")
          } yield message).getOrElse("null")
        }
      )(system.dispatcher)

    // t h e n
    (actual.futureValue should contain).theSameElementsAs(
      (1 to 10).map(n => s"""{"surname":"Integration","id":$n,"name":"Test"}""")
    )

  }

}

object SqsSourceIT {

  trait SqsSourceConfigurationIT
      extends AWSConfigurationIT
      with AnyFlatSpecLike
      with ScalaFutures
      with BeforeAndAfterEach
      with BeforeAndAfterAll
      with Matchers {

    implicit val defaultPatience: PatienceConfig =
      PatienceConfig(timeout = 60.seconds, interval = 200.millis)

    override protected def beforeAll(): Unit = {
      container = dockerInfrastructure
      container.start()
      initialization(system.dispatcher).futureValue
    }

    override protected def afterAll(): Unit = {
      system.terminate.futureValue
      awsSqsClient.close()
      awsSqsClient.close()
      container.stop()
    }

    override var container: containers.DockerComposeContainer[_] = _

    def initialization(implicit ec: ExecutionContextExecutor) =
      for {
        queue1Response <- createQueue("queue1")
        queue2Response <- createQueue("queue2.fifo", fifo = true)
        queue3Response <- createQueue("queue3")

        topicResponse <- createTopic("topic1")
        queue1Url      = queue1Response.queueUrl()
        queue2Url      = queue2Response.queueUrl()
        queue3Url      = queue3Response.queueUrl()
        topicArn       = topicResponse.topicArn()

        _      <- createSubscription(topicArn, queue1Url)
        actual <- createData()
      } yield (
        queue1Url shouldBe this.queue1Url,
        queue2Url shouldBe this.queue2Url,
        queue3Url shouldBe this.queue3Url,
        topicArn shouldBe this.topicArn,
        actual shouldBe Done
      )

    def createData(): Future[Done] =
      Source
        .single("{\"id\":1,\"name\":\"Test\"}")
        .map(SendMessageRequest.builder().messageBody(_).build())
        .via(SqsPublishFlow(queue1Url, SqsPublishSettings.create())(awsSqsClient))
        .concat(
          Source
            .fromIterator(() => (1 to 10).iterator)
            .map(n => s"""{\"id\":$n,\"name\":\"Test\"}""")
            .map(SendMessageRequest.builder().messageBody(_).messageGroupId("foo").build())
            .via(SqsPublishFlow(queue2Url, SqsPublishSettings.create())(awsSqsClient))
        )
        .concat(
          Source
            .fromIterator(() => (1 to 10).iterator)
            .map(n => s"""{\"id\":$n,\"name\":\"Test\"}""")
            .map(SendMessageRequest.builder().messageBody(_).build())
            .via(SqsPublishFlow(queue3Url, SqsPublishSettings.create())(awsSqsClient))
        )
        .runWith(Sink.foreach(println))

    protected def createSqsSource(queueUrl: String): Source[Message, NotUsed] =
      SqsSource(
        queueUrl,
        SqsSourceSettings().withCloseOnEmptyReceive(true)
      )(awsSqsClient)

  }

}
