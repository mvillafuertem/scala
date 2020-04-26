package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.model.Order.{ Bacon, Coffee, Sandwich }
import io.github.mvillafuertem.zio.queues.model.OrderGenerator
import zio._
import zio.clock.{ Clock, _ }
import zio.console.{ putStrLn, Console }
import zio.duration._
import zio.random.Random

case class Producer[A](producerSettings: ProducerSettings[A]) {

  def produce(generator: OrderGenerator[A]): URIO[Clock with Console with Random, Fiber.Runtime[Nothing, Nothing]] =
    (for {
      _ <- putStrLn(s"${scala.Console.CYAN}[${producerSettings.name}] ~ Generating Order ${scala.Console.RESET}")
      a <- generator.generate(Coffee)
      b <- generator.generate(Sandwich)
      c <- generator.generate(Bacon)
      _ <- producerSettings.topic.offer(a)
      _ <- producerSettings.topic.offer(b)
      _ <- producerSettings.topic.offer(c)
      // <- sleep(2.seconds)
    } yield ()).forever.fork

}

object Producer {
  def make[A](producerSettings: ProducerSettings[A]): UIO[Producer[A]] =
    UIO.succeed(Producer(producerSettings))
}
