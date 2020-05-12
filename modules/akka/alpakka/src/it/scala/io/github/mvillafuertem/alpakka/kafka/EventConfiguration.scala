package io.github.mvillafuertem.alpakka.kafka

import java.util.UUID

import akka.Done
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Transactional}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.Future

trait EventConfiguration {

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

  def source(consumerSettings: ConsumerSettings[String, String],
             consumerConfigurationProperties: KafkaConsumerConfigurationProperties): Source[ConsumerMessage.TransactionalMessage[String, String], Consumer.Control] =
    Transactional.source(consumerSettings, Subscriptions.topics(consumerConfigurationProperties.consumerTopic))

  def sink(producerSettings: ProducerSettings[String, String]): Sink[ProducerMessage.Envelope[String, String, ConsumerMessage.PartitionOffset], Future[Done]] =
    Transactional.sink(producerSettings, UUID.randomUUID().toString)

}
