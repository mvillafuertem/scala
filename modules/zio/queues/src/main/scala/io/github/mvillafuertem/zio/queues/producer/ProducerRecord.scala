package io.github.mvillafuertem.zio.queues.producer

case class ProducerRecord[T] (id: Int, value: T)
