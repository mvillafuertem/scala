package io.github.mvillafuertem.alpakka.sns

import akka.actor.ActorSystem
import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{ CreateTopicRequest, CreateTopicResponse, SubscribeRequest, SubscribeResponse }
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model._

import java.io.File
import java.net.URI
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

trait AWSConfigurationIT {

  private val uri                 = "http://localhost:4568"
  private val accessKey           = "andromedaUser"
  private val secretAccessKey     = "andromedaAccessKey"
  private val region: Region      = Region.US_EAST_1
  private val credentialsProvider = StaticCredentialsProvider
    .create(AwsBasicCredentials.create(accessKey, secretAccessKey))

  implicit val system: ActorSystem                = ActorSystem()
  private val asyncHttpClient: SdkAsyncHttpClient = AkkaHttpClient.builder().withActorSystem(system).build()

  protected val queue1Url = "http://localhost:4566/000000000000/queue1"
  protected val queue2Url = "http://localhost:4566/000000000000/queue2.fifo"
  protected val queue3Url = "http://localhost:4566/000000000000/queue3"
  protected val topicArn  = "arn:aws:sns:us-east-1:000000000000:topic1"

  implicit val awsSqsClient: SqsAsyncClient = SqsAsyncClient
    .builder()
    .credentialsProvider(credentialsProvider)
    .endpointOverride(URI.create(uri))
    .region(region)
    .httpClient(asyncHttpClient)
    .build()

  implicit val awsSnsClient: SnsAsyncClient =
    SnsAsyncClient
      .builder()
      .credentialsProvider(credentialsProvider)
      .endpointOverride(URI.create(uri))
      .region(region)
      .httpClient(asyncHttpClient)
      .build()

  protected def createQueue(queueName: String, fifo: Boolean = false): Future[CreateQueueResponse] =
    awsSqsClient
      .createQueue(
        CreateQueueRequest
          .builder()
          .queueName(queueName)
          .attributes(
            Map(
              QueueAttributeName.FIFO_QUEUE                  -> fifo.toString,
              QueueAttributeName.DELAY_SECONDS               -> "0",
              QueueAttributeName.MESSAGE_RETENTION_PERIOD    -> "86400",
              QueueAttributeName.CONTENT_BASED_DEDUPLICATION -> fifo.toString
            ).asJava
          )
          .build()
      )
      .toScala

  protected def createTopic(topicName: String): Future[CreateTopicResponse] =
    awsSnsClient
      .createTopic(
        CreateTopicRequest
          .builder()
          .name(topicName)
          .build()
      )
      .toScala

  protected def createSubscription(topicArn: String, endpoint: String, protocol: String = "sqs"): Future[SubscribeResponse] =
    awsSnsClient
      .subscribe(
        SubscribeRequest
          .builder()
          .protocol(protocol)
          .endpoint(endpoint)
          .returnSubscriptionArn(true)
          .topicArn(topicArn)
          .build()
      )
      .toScala

  var container: containers.DockerComposeContainer[_]

  def dockerInfrastructure: containers.DockerComposeContainer[_] =
    DockerComposeContainer(
      new File(s"modules/alpakka/sns/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("localstack", 4566, 1, Wait.forLogMessage(".*Ready.*", 1))),
      identifier = "docker_infrastructure"
    ).container

}
