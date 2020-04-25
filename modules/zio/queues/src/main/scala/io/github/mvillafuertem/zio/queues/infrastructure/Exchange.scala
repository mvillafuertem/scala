package io.github.mvillafuertem.zio.queues.infrastructure

import io.github.mvillafuertem.zio.queues.model.Order.{ Bacon, Coffee, Sandwich }
import io.github.mvillafuertem.zio.queues.model.OrderGenerator.Request
import zio.{ Fiber, Queue, UIO, ZIO }

case class Exchange[A]() {

  val jobQueueM: UIO[Queue[Request[A]]] = Queue.bounded[Request[A]](10)

  val kitchen: UIO[Queue[A]] = Queue.bounded[A](10)
  val bar: UIO[Queue[A]]     = Queue.bounded[A](10)

  def run: UIO[(Queue[Request[A]], Topic[A], Topic[A], Fiber.Runtime[Nothing, Nothing])] =
    for {
      jobQueue     <- jobQueueM
      kitchen      <- kitchen
      bar          <- bar
      kitchenTopic = Topic(kitchen, Map.empty)
      barTopic     = Topic(bar, Map.empty)
      loop = for {
        job <- jobQueue.take
        _ <- job.order match {
              case Coffee   => bar.offer(job.nOrder)
              case Sandwich => kitchen.offer(job.nOrder)
              case Bacon    => kitchen.offer(job.nOrder)
            }
      } yield ()
      fiber <- loop.forever.fork
    } yield (jobQueue, kitchenTopic, barTopic, fiber)
}

object Exchange {
  def createM[A]: UIO[Exchange[A]] = ZIO.succeed(Exchange[A]())
}
