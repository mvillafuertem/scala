package io.github.mvillafuertem.zio.queues.fibers.application

import zio.{ Queue, UIO, ZIO }

import scala.util.Random

case class TopicQueue[A](queue: Queue[A], subscribers: Map[Int, List[Queue[A]]]) {
  def subscribeM(sub: Queue[A], consumerGroup: Int): UIO[TopicQueue[A]] = {
    val updatedMap = subscribers.get(consumerGroup) match {
      case Some(value) =>
        subscribers + (consumerGroup -> (value :+ sub))
      case None =>
        subscribers + (consumerGroup -> List(sub))
    }

    UIO.succeed(copy(subscribers = updatedMap))
  }

  def run = {
    def randomElement(list: List[Queue[A]]) =
      if (list.nonEmpty) {
        Some(list(Random.nextInt(list.length)))
      } else {
        None
      }
    val loop = for {
      elem   <- queue.take
      mapped = subscribers.values.flatMap(randomElement(_).map(_.offer(elem)))
      _      <- ZIO.collectAll(mapped)
    } yield ()
    loop.forever.fork
  }

}

object TopicQueue {
  def createM[A](queue: Queue[A]): UIO[TopicQueue[A]] = UIO.succeed(TopicQueue(queue, Map.empty))
}
