package io.github.mvillafuertem.alpakka.kafka

import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.alpakka.kafka.KafkaConsumerConfigurationProperties._


final case class KafkaConsumerConfigurationProperties
(
  config: Config = ConfigFactory.load().getConfig(KafkaConsumerConfigurationProperties.path),
  bootstrapServers: String = ConfigFactory.load().getString(s"$path.bootstrap-servers"),
  groupId: String = ConfigFactory.load().getString(s"$path.group-id"),
  autoOffsetReset: String = ConfigFactory.load().getString(s"$path.auto-offset-reset"),
  enableAutoCommit: String = ConfigFactory.load().getString(s"$path.enable-auto-commit"),
  autoCommitIntervalMs: String = ConfigFactory.load().getString(s"$path.auto-commit-interval-ms"),
  consumerTopic: String = ConfigFactory.load().getString(s"$path.consumer-topic")
)

object KafkaConsumerConfigurationProperties {

  private val path: String = "infrastructure.kafka.consumer"

}
