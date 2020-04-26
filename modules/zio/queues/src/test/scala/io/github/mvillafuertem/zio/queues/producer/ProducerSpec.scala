package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.model.OrderGenerator.{IntRequestGenerator, Request}
import org.scalatest.Ignore
import zio.Queue
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{TestClock, TestConsole, TestEnvironment, TestRandom}

import scala.Console.{CYAN, RESET}

@Ignore
object ProducerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(produce) @@ TestAspect.ignore

  lazy val produce =
    testM("produce") {
      assertM(
        for {
          _         <- TestRandom.feedInts(1, 2, 3)
          topic     <- Queue.bounded[Request[Int]](10)
          generator = IntRequestGenerator()
          producer  <- Producer.make[Int](ProducerSettings("Test", topic))
          p         <- producer.produce(generator)
          _ <- TestClock.adjust(2.second)
          _ <- p.interrupt.awaitAllChildren
          s         <- TestClock.sleeps
          output    <- TestConsole.output
        } yield (output, s)
        // t h e n
      )(
        equalTo(
          (
            Vector(
              s"$CYAN[Test] ~ Generating Order $RESET\n",
              s"$CYAN     1 ~ Request Coffee $RESET\n",
              s"$CYAN     2 ~ Request Sandwich $RESET\n",
              s"$CYAN     3 ~ Request Bacon $RESET\n"
            ),
            List()
          )
        )
      )
    }

}
