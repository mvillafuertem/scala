package io.github.mvillafuertem.zio.queues.producer

import zio.{ Has, Queue }

case class ProducerSettings[A](name: String, topic: Queue[ProducerRecord[A]])

object ProducerSettings {

  type ZProducerSettings[A] = Has[ProducerSettings[A]]

}
