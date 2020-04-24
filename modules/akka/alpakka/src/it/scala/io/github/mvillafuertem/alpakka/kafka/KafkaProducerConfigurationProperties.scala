package io.github.mvillafuertem.alpakka.kafka

import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.alpakka.kafka.KafkaProducerConfigurationProperties._

final case class KafkaProducerConfigurationProperties
(
  config: Config = ConfigFactory.load().getConfig(KafkaProducerConfigurationProperties.path),
  bootstrapServers: String = ConfigFactory.load().getString(s"$path.bootstrap-servers"),
  transactionalId: String = ConfigFactory.load().getString(s"$path.transactional-id"),
  producerTopic: String = ConfigFactory.load().getString(s"$path.producer-topic")
)

object KafkaProducerConfigurationProperties {

  private val path: String = "infrastructure.kafka.producer"

}
