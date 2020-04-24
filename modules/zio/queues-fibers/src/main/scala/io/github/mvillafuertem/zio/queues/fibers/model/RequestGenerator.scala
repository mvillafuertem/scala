package io.github.mvillafuertem.zio.queues.fibers.model

import io.github.mvillafuertem.zio.queues.fibers.model.RequestGenerator.Request

import scala.util.Random

trait RequestGenerator[A] {
  def generate(topic: Diagnostic): Request[A]
}

object RequestGenerator {

  case class Request[A](topic: Diagnostic, XRayImage: A)

  case class IntRequestGenerator() extends RequestGenerator[Int] {
    override def generate(topic: Diagnostic): Request[Int] =
      Request(topic, Random.nextInt(1000))
  }

}
