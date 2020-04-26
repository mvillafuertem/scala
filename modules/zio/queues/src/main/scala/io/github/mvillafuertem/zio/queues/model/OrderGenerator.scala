package io.github.mvillafuertem.zio.queues.model

import io.github.mvillafuertem.zio.queues.model.OrderGenerator.Request
import zio.console._
import zio.random._
import zio.{ random, UIO, ZIO }

trait OrderGenerator[A] {
  def generate(topic: Order): ZIO[Console with Random, Nothing, Request[A]]
}

object OrderGenerator {

  case class Request[A](order: Order, nOrder: A)

  case class IntRequestGenerator() extends OrderGenerator[Int] {
    override def generate(order: Order): ZIO[Console with Random, Nothing, Request[Int]] =
      for {
        nOrder  <- nextInt(1000)
        _       <- putStrLn(s"${scala.Console.CYAN}     $nOrder ~ Request $order ${scala.Console.RESET}")
        request <- UIO.effectTotal(Request(order, nOrder))
      } yield request

  }

}
