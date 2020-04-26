package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.model.Order.{ Bacon, Coffee, Sandwich }
import io.github.mvillafuertem.zio.queues.model.OrderGenerator.{ IntRequestGenerator, Request }
import zio.Queue
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestEnvironment, TestRandom }

object ProducerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(produce)

  lazy val produce =
    testM("produce") {
      assertM(
        for {
          _         <- TestRandom.feedInts(1, 2, 3)
          topic     <- Queue.bounded[Request[Int]](10)
          generator = IntRequestGenerator()
          producer  <- Producer.make[Int](ProducerSettings("Test", topic))
          _         <- TestClock.adjust(2.seconds)
          p         <- producer.produce(generator)
          a         <- topic.take
          b         <- topic.take
          c         <- topic.take
          _         <- p.interrupt.awaitAllChildren
        } yield List(a, b, c)
        // t h e n
      )(equalTo(List(Request(Coffee, 1), Request(Sandwich, 2), Request(Bacon, 3))))
    }

}
