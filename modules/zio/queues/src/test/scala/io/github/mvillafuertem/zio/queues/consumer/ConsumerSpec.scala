package io.github.mvillafuertem.zio.queues.consumer

import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestConsole, TestEnvironment, TestRandom }

import scala.Console._

object ConsumerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      consume,
      consumeWithSleep
    )

  lazy val consume =
    testM("consume") {
      assertM(
        for {
          _              <- TestRandom.feedInts(1)
          consumer       <- Consumer.make[Int](ConsumerSettings("Test", 1))
          c              <- consumer.consume()
          (queue, fiber) = c
          _              <- queue.offer(1)
          _              <- TestClock.adjust(1.seconds)
          _              <- fiber.interrupt.awaitAllChildren
          sleep          <- TestClock.sleeps
          output         <- TestConsole.output
        } yield (output, sleep)
        // t h e n
      )(equalTo((Vector(s"$GREEN[Test] worker: Starting preparing order 1$RESET\n", s"$GREEN[Test] worker: Finished order 1$RESET\n"), List())))
    }

  lazy val consumeWithSleep =
    testM("consumeWithSleep") {
      assertM(
        for {
          _              <- TestRandom.feedInts(1)
          consumer       <- Consumer.make[Int](ConsumerSettings("Test", 1))
          c              <- consumer.consume()
          (queue, fiber) = c
          _              <- queue.offer(1)
          _              <- fiber.interrupt.awaitAllChildren
          sleep          <- TestClock.sleeps
          output         <- TestConsole.output
        } yield (output, sleep)
        // t h e n
      )(equalTo((Vector(s"$GREEN[Test] worker: Starting preparing order 1$RESET\n"), List(1.second))))
    }

}
