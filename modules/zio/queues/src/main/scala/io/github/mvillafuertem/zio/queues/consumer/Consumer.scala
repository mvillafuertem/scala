package io.github.mvillafuertem.zio.queues.consumer

import java.util.concurrent.TimeUnit

import ConsumerSettings.ZConsumerSettings
import zio.{ Queue, _ }
import zio.clock._
import zio.console.{ putStrLn, Console }
import zio.duration.Duration

import scala.util.Random

trait Consumer[A] {

  def consume(): ZIO[Console with Clock, Nothing, (Queue[A], Fiber.Runtime[Nothing, Nothing])]

}

object Consumer {

  def make[A](consumerSettings: ConsumerSettings): UIO[Consumer[A]] =
    UIO.succeed(Live[A](consumerSettings))

  type ZConsumer[A] = Has[Consumer[A]]

  case class Live[A](consumerSettings: ConsumerSettings) extends Consumer[A] {

    private val queueM: UIO[Queue[A]] = Queue.bounded[A](consumerSettings.size)

    def consume(): ZIO[Console with Clock, Nothing, (Queue[A], Fiber.Runtime[Nothing, Nothing])] =
      for {
        queue <- queueM
        fiber <- loop(queue).forever.fork
      } yield (queue, fiber)

    def loop(queue: Queue[A]) =
      for {
        nOrder <- queue.take
        _      <- putStrLn(s"${consumerSettings.color}[${consumerSettings.name}] worker: Starting preparing order $nOrder${scala.Console.RESET}")
        _      <- sleep(Duration(Random.nextInt(4), TimeUnit.SECONDS))
        _      <- putStrLn(s"${consumerSettings.color}[${consumerSettings.name}] worker: Finished order $nOrder${scala.Console.RESET}")
      } yield ()

  }

  def live[A: Tagged]: ZLayer[ZConsumerSettings, Nothing, ZConsumer[A]] =
    ZLayer.fromService[ConsumerSettings, Consumer[A]](Live.apply[A])

  def makeM[A: Tagged](consumerSettings: ConsumerSettings): ZLayer[Any, Nothing, ZConsumer[A]] =
    ZLayer.succeed(consumerSettings) >>> live[A]

}
