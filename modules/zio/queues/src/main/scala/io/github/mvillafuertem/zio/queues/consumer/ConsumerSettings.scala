package io.github.mvillafuertem.zio.queues.consumer

import zio.Has

case class ConsumerSettings(name: String, size: Int, color: String = Console.GREEN)

object ConsumerSettings {

  type ZConsumerSettings = Has[ConsumerSettings]

}
