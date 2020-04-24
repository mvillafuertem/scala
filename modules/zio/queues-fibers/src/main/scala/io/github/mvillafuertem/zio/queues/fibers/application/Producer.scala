package io.github.mvillafuertem.zio.queues.fibers.application

import java.util.concurrent.TimeUnit

import io.github.mvillafuertem.zio.queues.fibers.model.Diagnostic.{ HipDiagnostic, KneeDiagnostic }
import io.github.mvillafuertem.zio.queues.fibers.model.RequestGenerator
import io.github.mvillafuertem.zio.queues.fibers.model.RequestGenerator.Request
import zio.console.putStrLn
import zio.duration.Duration
import zio.{ Queue, UIO, ZIO }

case class Producer[A](queue: Queue[Request[A]], generator: RequestGenerator[A]) {
  def run = {
    val loop = for {
      _ <- putStrLn("[XRayRoom] generating hip and knee request")
      _ <- queue.offer(generator.generate(HipDiagnostic))
      _ <- queue.offer(generator.generate(KneeDiagnostic))
      _ <- ZIO.sleep(Duration(2, TimeUnit.SECONDS))
    } yield ()
    loop.forever.fork
  }
}

object Producer {
  def createM[A](queue: Queue[Request[A]], generator: RequestGenerator[A]) =
    UIO.succeed(Producer(queue, generator))
}
