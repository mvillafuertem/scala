package io.github.mvillafuertem.zio.queues.consumer

import io.github.mvillafuertem.zio.queues.RestaurantOrderExchangeApp.{Bacon, Order}
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{TestClock, TestConsole, TestEnvironment, TestRandom}

import scala.Console._

object ConsumerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      consume
    ) @@ TestAspect.timed @@ TestAspect.ignore

  lazy val consume =
    testM("consume") {
      assertM(
        for {
          _              <- TestRandom.feedInts(1)
          consumer       <- Consumer.make[Order](ConsumerSettings("Test", 1))
          c              <- consumer.consume()
          (queue, fiber) = c
          _              <- queue.offer(ConsumerRecord(1, Bacon))
          _              <- TestClock.adjust(2.seconds)
          _              <- fiber.interrupt.awaitAllChildren
          sleep          <- TestClock.sleeps
          output         <- TestConsole.output
        } yield (output, sleep)
        // t h e n
      )(equalTo((Vector(s"$GREEN[Test] worker: Starting preparing order ConsumerRecord(1,Bacon)$RESET\n", s"$GREEN[Test] worker: Finished order ConsumerRecord(1,Bacon)$RESET\n"), List())))
    }

}
