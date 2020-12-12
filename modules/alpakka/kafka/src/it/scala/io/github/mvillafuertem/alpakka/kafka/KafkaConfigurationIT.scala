package io.github.mvillafuertem.alpakka.kafka

import java.io.File
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.kafka._
import akka.kafka.scaladsl.{ Consumer, Transactional }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import com.dimafeng.testcontainers.{ DockerComposeContainer, ExposedService }
import io.github.mvillafuertem.alpakka.kafka.properties.{ KafkaConsumerConfigurationProperties, KafkaProducerConfigurationProperties }
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.{ ExecutionContextExecutor, Future }

trait KafkaConfigurationIT {

  implicit val actorSystem: ActorSystem                           = ActorSystem("KafkaConnectionCheckerSpec")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val actorMaterializer: ActorMaterializer               = ActorMaterializer()
  implicit val log: LoggingAdapter                                = Logging(actorSystem, classOf[KafkaConfigurationIT])

  val consumerConfigurationProperties: KafkaConsumerConfigurationProperties = KafkaConsumerConfigurationProperties()
  val producerConfigurationProperties: KafkaProducerConfigurationProperties = KafkaProducerConfigurationProperties()
  val producerSettings: ProducerSettings[String, String]                    = createProducerSettings(producerConfigurationProperties)
  val consumerSettings: ConsumerSettings[String, String]                    = createConsumerSettings(KafkaConsumerConfigurationProperties())

  var container: containers.DockerComposeContainer[_]

  def dockerInfrastructure: containers.DockerComposeContainer[_] =
    DockerComposeContainer(
      new File(s"modules/alpakka/kafka/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("kafka", 9092, 1, Wait.forLogMessage(".*started .*\\n", 1))),
      identifier = "docker_infrastructure"
    ).container

  def createProducerSettings(producerConfigurationProperties: KafkaProducerConfigurationProperties): ProducerSettings[String, String] =
    ProducerSettings(KafkaProducerConfigurationProperties.default, new StringSerializer, new StringSerializer)
      .withBootstrapServers(producerConfigurationProperties.bootstrapServers)

  def createConsumerSettings(consumerConfigurationProperties: KafkaConsumerConfigurationProperties): ConsumerSettings[String, String] =
    ConsumerSettings(KafkaConsumerConfigurationProperties.default, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(consumerConfigurationProperties.bootstrapServers)
      .withGroupId(consumerConfigurationProperties.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerConfigurationProperties.autoOffsetReset)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerConfigurationProperties.enableAutoCommit)
      .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerConfigurationProperties.autoCommitIntervalMs)
      .withProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed")

  def source(
    consumerSettings: ConsumerSettings[String, String],
    consumerConfigurationProperties: KafkaConsumerConfigurationProperties
  ): Source[ConsumerMessage.TransactionalMessage[String, String], Consumer.Control] =
    Transactional.source(consumerSettings, Subscriptions.topics(consumerConfigurationProperties.consumerTopic))

  def sink(producerSettings: ProducerSettings[String, String]): Sink[ProducerMessage.Envelope[String, String, ConsumerMessage.PartitionOffset], Future[Done]] =
    Transactional.sink(producerSettings, UUID.randomUUID().toString)

}
