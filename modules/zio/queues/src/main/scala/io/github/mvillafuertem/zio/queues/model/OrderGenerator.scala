package io.github.mvillafuertem.zio.queues.model

import OrderGenerator.Request

import scala.util.Random

trait OrderGenerator[A] {
  def generate(topic: Order): Request[A]
}

object OrderGenerator {

  case class Request[A](order: Order, nOrder: A)

  case class IntRequestGenerator() extends OrderGenerator[Int] {
    override def generate(order: Order): Request[Int] = {
      val nOrder = Random.nextInt(1000)
      print(scala.Console.CYAN)
      println(s"     $nOrder ~ Request $order ")
      print(scala.Console.RESET)
      Request(order, nOrder)
    }
  }

}
