package io.github.mvillafuertem.alpakka.kafka.properties

import com.typesafe.config.{ Config, ConfigFactory }

final case class KafkaConsumerConfigurationProperties(
  bootstrapServers: String,
  groupId: String,
  autoOffsetReset: String,
  enableAutoCommit: String,
  autoCommitIntervalMs: String,
  consumerTopic: String
) {

  def withBootstrapServers(bootstrapServers: String): KafkaConsumerConfigurationProperties =
    copy(bootstrapServers = bootstrapServers)

  def withGroupId(groupId: String): KafkaConsumerConfigurationProperties =
    copy(groupId = groupId)

  def withAutoOffsetReset(autoOffsetReset: String): KafkaConsumerConfigurationProperties =
    copy(autoOffsetReset = autoOffsetReset)

  def withEnableAutoCommit(enableAutoCommit: String): KafkaConsumerConfigurationProperties =
    copy(enableAutoCommit = enableAutoCommit)

  def withAutoCommitIntervalMs(autoCommitIntervalMs: String): KafkaConsumerConfigurationProperties =
    copy(autoCommitIntervalMs = autoCommitIntervalMs)

  def withConsumerTopic(consumerTopic: String): KafkaConsumerConfigurationProperties =
    copy(consumerTopic = consumerTopic)

}

object KafkaConsumerConfigurationProperties {

  val default: Config = ConfigFactory.load().getConfig("infrastructure.kafka.consumer")

  def apply(config: Config = default): KafkaConsumerConfigurationProperties =
    new KafkaConsumerConfigurationProperties(
      bootstrapServers = config.getString("bootstrap-servers"),
      groupId = config.getString("group-id"),
      autoOffsetReset = config.getString("auto-offset-reset"),
      enableAutoCommit = config.getString("enable-auto-commit"),
      autoCommitIntervalMs = config.getString("auto-commit-interval-ms"),
      consumerTopic = config.getString("consumer-topic")
    )

}
