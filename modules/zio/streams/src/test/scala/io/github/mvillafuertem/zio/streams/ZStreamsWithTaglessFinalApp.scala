package io.github.mvillafuertem.zio.streams

import zio._
import zio.stream.ZStream
import zio.test.Assertion.{ equalTo, fails }
import zio.test._
import zio.test.environment.TestEnvironment

object ZStreamsWithTaglessFinalApp extends DefaultRunnableSpec {

  trait Interface[F[_]] {

    def acc(a: Int, b: Int): F[Int]

    def div(a: Int, b: Int): F[Int]

  }

  sealed trait Error
  final case object DivError extends Error

  type Monad[T] = stream.ZStream[_, _, T]
  final class Impl extends Interface[Monad] {

    override def acc(a: Int, b: Int): ZStream[Any, Nothing, Int] =
      stream.Stream.succeed(a + b)

    override def div(a: Int, b: Int): ZStream[Any, Error, Int] =
      stream.Stream.fromEffect(Task.effect(a / b)).mapError(_ => DivError)

  }

  object Impl {

    def apply(): Impl = new Impl()

    type ZImpl = Has[Impl]

    def acc(a: Int, b: Int): stream.ZStream[ZImpl, Nothing, Int] =
      stream.ZStream.accessStream(_.get.acc(a, b))

    def div(a: Int, b: Int): stream.ZStream[ZImpl, Error, Int] =
      stream.ZStream.accessStream(_.get.div(a, b))

    val live: Layer[Nothing, ZImpl] = ZLayer.succeed(Impl())

  }

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("ZStreams With Tagless-Final")(
      testM("acc")(
        assertM(Impl.acc(2, 3).runCollect)(equalTo(Seq(5)))
      ),
      testM("div")(
        assertM(Impl.div(1, 0).runCollect.run)(fails(equalTo(DivError)))
      )
    ).provideLayer(Impl.live)

}
