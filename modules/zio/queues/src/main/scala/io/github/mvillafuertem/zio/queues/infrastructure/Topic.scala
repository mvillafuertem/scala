package io.github.mvillafuertem.zio.queues.infrastructure

import io.github.mvillafuertem.zio.queues.consumer.ConsumerRecord
import zio._

import scala.util.Random

trait Topic[A] {

  def subscribeM(consumer: Queue[ConsumerRecord[A]], consumerGroup: Int): UIO[Topic[A]]

  def run: URIO[Any, Fiber.Runtime[Nothing, Nothing]]

}

object Topic {

  type ZTopic[A] = Has[Topic[A]]

  def createM[A](queue: Queue[ConsumerRecord[A]]): UIO[Topic[A]] = UIO.succeed(Live(queue, Map.empty))

  case class Live[A](consumer: Queue[ConsumerRecord[A]], subscribers: Map[Int, List[Queue[ConsumerRecord[A]]]]) extends Topic[A] {
    def subscribeM(consumer: Queue[ConsumerRecord[A]], consumerGroup: Int): UIO[Topic[A]] = {
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

    def randomElement(list: List[Queue[ConsumerRecord[A]]]): Option[Queue[ConsumerRecord[A]]] =
      if (list.nonEmpty) {
        Some(list(Random.nextInt(list.length)))
      } else {
        None
      }

  }

  def live[A: Tagged]: ZLayer[Has[Queue[ConsumerRecord[A]]], Nothing, ZTopic[A]] =
    ZLayer.fromService[Queue[ConsumerRecord[A]], Topic[A]](queue => Live.apply[A](queue, Map.empty))

  def makeM[A: Tagged](queue: Queue[ConsumerRecord[A]]): ZLayer[Any, Nothing, ZTopic[A]] =
    ZLayer.succeed(queue) >>> live[A]

}
