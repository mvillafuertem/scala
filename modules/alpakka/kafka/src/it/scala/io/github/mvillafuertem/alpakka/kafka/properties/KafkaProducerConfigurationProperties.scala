package io.github.mvillafuertem.alpakka.kafka.properties

import com.typesafe.config.{ Config, ConfigFactory }

final case class KafkaProducerConfigurationProperties(
  bootstrapServers: String,
  transactionalId: String,
  producerTopic: String
) {

  def withBootstrapServers(bootstrapServers: String): KafkaProducerConfigurationProperties =
    copy(bootstrapServers = bootstrapServers)

  def withTransactionalId(transactionalId: String): KafkaProducerConfigurationProperties =
    copy(transactionalId = transactionalId)

  def withProducerTopic(producerTopic: String): KafkaProducerConfigurationProperties =
    copy(producerTopic = producerTopic)

}

object KafkaProducerConfigurationProperties {

  val default: Config = ConfigFactory.load().getConfig("infrastructure.kafka.producer")

  def apply(config: Config = default): KafkaProducerConfigurationProperties =
    new KafkaProducerConfigurationProperties(
      bootstrapServers = config.getString("bootstrap-servers"),
      transactionalId = config.getString("transactional-id"),
      producerTopic = config.getString("producer-topic")
    )
}
