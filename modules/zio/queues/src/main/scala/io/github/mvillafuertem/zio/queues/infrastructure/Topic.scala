package io.github.mvillafuertem.zio.queues.infrastructure

import zio.{ Fiber, Queue, UIO, URIO, ZIO }

import scala.util.Random

case class Topic[A](consumer: Queue[A], subscribers: Map[Int, List[Queue[A]]]) {
  def subscribeM(consumer: Queue[A], consumerGroup: Int): UIO[Topic[A]] = {
    val updatedMap = subscribers.get(consumerGroup) match {
      case Some(value) =>
        subscribers + (consumerGroup -> (value :+ consumer))
      case None =>
        subscribers + (consumerGroup -> List(consumer))
    }

    UIO.succeed(copy(subscribers = updatedMap))
  }

  def run: URIO[Any, Fiber.Runtime[Nothing, Nothing]] = {
    val loop = for {
      elem <- consumer.take
      mapped = subscribers.values
        .flatMap(
          randomElement(_)
            .map(_.offer(elem))
        )
      _ <- ZIO.collectAll(mapped)
    } yield ()
    loop.forever.fork
  }

  def randomElement(list: List[Queue[A]]): Option[Queue[A]] =
    if (list.nonEmpty) {
      Some(list(Random.nextInt(list.length)))
    } else {
      None
    }

}

object Topic {
  def createM[A](queue: Queue[A]): UIO[Topic[A]] = UIO.succeed(Topic(queue, Map.empty))
}
