package io.github.mvillafuertem.zio.queues.fibers.application

import java.util.concurrent.TimeUnit

import zio.console.putStrLn
import zio.duration.Duration
import zio.{ Queue, UIO, ZIO }

import scala.util.Random

case class Consumer[A](title: String) {
  val queueM = Queue.bounded[A](10)

  def run =
    for {
      queue <- queueM
      loop = for {
        img <- queue.take
        _   <- putStrLn(s"[$title] worker: Starting analyzing task $img")
        _   <- ZIO.sleep(Duration(Random.nextInt(4), TimeUnit.SECONDS))
        _   <- putStrLn(s"[$title] worker: Finished task $img")
      } yield ()
      fiber <- loop.forever.fork
    } yield (queue, fiber)
}

object Consumer {
  def createM[A](title: String) = UIO.succeed(Consumer[A](title))
}
