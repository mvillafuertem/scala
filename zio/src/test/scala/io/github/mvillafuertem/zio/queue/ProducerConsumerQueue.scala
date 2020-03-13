package io.github.mvillafuertem.zio.queue

import io.github.mvillafuertem.zio.queue.ProducerConsumerQueue.MainEnv
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.duration._
import zio.{BootstrapRuntime, Queue, ZIO}

final class ProducerConsumerQueue extends BootstrapRuntime
  with AnyFlatSpecLike with Matchers {

  "A Queue" should "be shared by a producer and a consumer" in {

    val producer = for { // task to add something to the queue
      env <- ZIO.environment[MainEnv]
      queue = env.mainQueue
      id <- ZIO.descriptor
      _ <- putStrLn(s"Producer ~ fiber $id ~ queue $queue")
      _ <- ZIO.sleep(1 seconds)
      _ <- queue.offer("Give me Coffee!")
      id <- ZIO.descriptor
      _ <- putStrLn(s"offer $id")
      _ <- ZIO.sleep(2 seconds)
     } yield ()

    val consumer = for { // task to remove+print stuff from queue
      env <- ZIO.environment[MainEnv]
      queue = env.mainQueue
      id <- ZIO.descriptor
      //_ <- ZIO.sleep(10 seconds)
      _ <- putStrLn(s"Consumer ~ fiber $id ~ queue $queue")
      _ <- queue.take.flatMap(putStrLn).forever
     } yield ()

    val program: ZIO[Any, Nothing, Int] = ZIO.runtime[MainEnv] // top level to run both tasks
      .flatMap { _ =>
        for {
          _ <- producer.forever.fork
          id <- ZIO.descriptor
          _ <- putStrLn("Main " + id.toString)
          _ <- consumer //.forever
        } yield ()
      }.provideSomeM(
      for {
        q <- Queue.unbounded[String]
      } yield new MainEnv with Console.Live with Clock.Live {
        override val mainQueue: Queue[String] = q
      }).as(0)

    unsafeRun(program) shouldBe 0
  }

}

object ProducerConsumerQueue {

  trait MainEnv extends Console with Clock // environment with queue
  {
    val mainQueue: Queue[String]
  }


}
