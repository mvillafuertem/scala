package io.github.mvillafuertem.zio.queues.consumer

case class ConsumerRecord[T](id: Int, value: T)
