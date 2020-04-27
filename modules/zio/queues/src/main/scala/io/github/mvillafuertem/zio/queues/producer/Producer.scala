package io.github.mvillafuertem.zio.queues.producer

import io.github.mvillafuertem.zio.queues.producer.ProducerSettings.ZProducerSettings
import zio._
import zio.clock.Clock
import zio.console.{ putStrLn, Console }
import zio.random.Random
import zio.stream.{ Stream, ZStream, ZStreamChunk }

trait Producer[T] {

  def produce(producerRecord: ProducerRecord[T]): URIO[Console, Boolean]

  def produceChunk(it: Iterable[ProducerRecord[T]]): ZStream[Clock with Console with Random, Nothing, Boolean]

  def produceMPar: Stream[Throwable, ProducerRecord[T]] => ZStream[Console, Throwable, Boolean]

}

object Producer {

  type ZProducer[A] = Has[Producer[A]]

  case class Live[A](producerSettings: ProducerSettings[A]) extends Producer[A] {

    def produce(producerRecord: ProducerRecord[A]): URIO[Console, Boolean] =
      for {
        _      <- putStrLn(s"${scala.Console.CYAN}[${producerSettings.name}] ~ Generating ${producerRecord.value} ${scala.Console.RESET}")
        result <- producerSettings.topic.offer(producerRecord)
      } yield result

    def produceChunk(it: Iterable[ProducerRecord[A]]): ZStream[Clock with Console with Random, Nothing, Boolean] =
      ZStreamChunk
        .fromChunks(Chunk.fromIterable(it))
        .flattenChunks
        .mapM(produce)

    def produceMPar: Stream[Throwable, ProducerRecord[A]] => ZStream[Console, Throwable, Boolean] =
      stream => stream.mapMPar(3)(produce)

  }

  def make[T](producerSettings: ProducerSettings[T]): UIO[Producer[T]] =
    UIO.succeed(Live(producerSettings))

  def live[A: Tagged]: ZLayer[ZProducerSettings[A], Nothing, ZProducer[A]] =
    ZLayer.fromService[ProducerSettings[A], Producer[A]](Live.apply[A])

  def makeM[A: Tagged](producerSettings: ProducerSettings[A]): ZLayer[Any, Nothing, ZProducer[A]] =
    ZLayer.succeed(producerSettings) >>> live[A]

}
