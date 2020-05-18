package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.RestaurantOrderExchangeApp.{Bacon, Coffee, Order, Sandwich}
import zio.clock.Clock
import zio.console.Console
import zio.random.Random
import zio.stream._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{ExecutionStrategy, ZQueue}
object ProducerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      produce,
      produceChunk,
      produceMPar
    ) @@ TestAspect.timed

  lazy val produce: ZSpec[Console, Nothing] =
    testM("produce") {
      assertM(
        for {
          topic    <- ZQueue.bounded[ProducerRecord[Order]](3)
          producer <- Producer.make[Order](ProducerSettings("Test", topic))
          _        <- producer.produce(ProducerRecord(1, Coffee))
          _        <- producer.produce(ProducerRecord(2, Sandwich))
          _        <- producer.produce(ProducerRecord(3, Bacon))
          a        <- topic.take
          b        <- topic.take
          c        <- topic.take
        } yield List(a, b, c)
        // t h e n
      )(equalTo(List(ProducerRecord[Order](1, Coffee), ProducerRecord[Order](2, Sandwich), ProducerRecord[Order](3, Bacon))))
    }

  lazy val produceChunk: ZSpec[Clock with Console with Random, Nothing] =
    testM("produce chunk") {
      assertM(
        for {
          topic    <- ZQueue.bounded[ProducerRecord[Order]](3)
          iterable  = (1 to 3).map(n => ProducerRecord[Order](n, Coffee))
          producer <- Producer.make[Order](ProducerSettings("Test", topic))
          result   <- producer.produceChunk(iterable).runCollect
          a        <- topic.take
          b        <- topic.take
          c        <- topic.take
        } yield (result, List(a, b, c))
        // t h e n
      )(equalTo((List.fill(3)(true), (1 to 3).map(n => ProducerRecord[Order](n, Coffee)).toList)))
    }

  lazy val produceMPar: ZSpec[Annotations with Console, Throwable] =
    testM("produceMPar") {
      assertM(
        for {
          topic    <- ZQueue.bounded[ProducerRecord[Order]](3)
          iterable  = (1 to 3).map(n => ProducerRecord[Order](n, Bacon))
          producer <- Producer.make[Order](ProducerSettings("Test", topic))
          result   <- Stream.fromIterable(iterable).via(producer.produceMPar).runCollect
          records  <- Stream.fromQueue(topic).take(3).runCollect
        } yield (result, records)
        // t h e n
      )(equalTo((List.fill(3)(true), (1 to 3).map(n => ProducerRecord[Order](n, Bacon)).toList)))
    } @@ TestAspect.ignore

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential))

}
