package io.github.mvillafuertem.zio.queues.producer

import java.util.concurrent.TimeUnit

import io.github.mvillafuertem.zio.queues.model.Order.{ Bacon, Coffee, Sandwich }
import io.github.mvillafuertem.zio.queues.model.OrderGenerator
import zio._
import zio.clock.Clock
import zio.console.{ putStrLn, Console }
import zio.duration.Duration

case class Producer[A](producerSettings: ProducerSettings[A]) {

  def produce(generator: OrderGenerator[A]): URIO[Clock with Console, Fiber.Runtime[Nothing, Nothing]] = {
    val loop = for {
      _ <- console.putStr(scala.Console.CYAN)
      _ <- putStrLn(s"[${producerSettings.name}] ~ Generating Order ")
      _ <- console.putStr(scala.Console.RESET)
      _ <- producerSettings.topic.offer(generator.generate(Coffee))
      _ <- producerSettings.topic.offer(generator.generate(Sandwich))
      _ <- producerSettings.topic.offer(generator.generate(Bacon))
      _ <- ZIO.sleep(Duration(2, TimeUnit.SECONDS))
    } yield ()
    loop.forever.fork
  }
}

object Producer {
  def make[A](producerSettings: ProducerSettings[A]): UIO[Producer[A]] =
    UIO.succeed(Producer(producerSettings))
}
