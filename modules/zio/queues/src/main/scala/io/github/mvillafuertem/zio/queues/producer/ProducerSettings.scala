package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.model.OrderGenerator.Request
import zio.Queue

case class ProducerSettings[A](name: String, topic: Queue[Request[A]])
