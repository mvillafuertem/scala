package io.github.mvillafuertem.zio.queues.consumer

import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestConsole, TestEnvironment, TestRandom }

import scala.Console._

object ConsumerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(consume)

  lazy val consume =
    testM("consume") {
      assertM(
        for {
          sleeps         <- TestClock.sleeps
          _              <- TestRandom.feedInts(1)
          consumer       <- Consumer.make[Int](ConsumerSettings("Test", 1))
          c              <- consumer.consume()
          (queue, fiber) = c
          _              <- queue.offer(1)
          _              <- TestClock.adjust(5.seconds)
          _              <- fiber.interrupt.awaitAllChildren
          output         <- TestConsole.output
        } yield output
        // t h e n
      )(equalTo(Vector(s"$GREEN[Test] worker: Starting preparing order 1$RESET\n", s"$GREEN[Test] worker: Finished order 1$RESET\n")))
    }

}
