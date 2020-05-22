//package io.github.mvillafuertem.zio.streams
//
//import zio._
//import zio.stream.ZStream
//import zio.test.Assertion.{equalTo, fails}
//import zio.test._
//import zio.test.environment.TestEnvironment
//
//object ZStreamsTypedWithTaglessFinalApp extends DefaultRunnableSpec {
//
//  trait Interface[F[_]] {
//
//    def acc[T](a: T, b: T): F[T]
//
//    def div[T](a: T, b: T): F[T]
//
//  }
//
//  trait InterfaceBridge[A] {
//    def apply[F[_]](implicit L: Interface[F]): F[A]
//  }
//
//  sealed trait Error
//  final case object DivError extends Error
//
//  type Monad[R, E, O] = stream.ZStream[R, E, O]
//  final class Impl extends InterfaceBridge[Int] {
//    override def apply[F[_]](implicit L: Interface[F]): F[Int] = L.acc()
//  }
//
//  object Impl {
//
//    def apply(): Impl = new Impl()
//
//    type ZImpl[T, F[_]] = Has[Interface[T, F]]
//
//    def acc(a: Int, b: Int)(implicit L: ZImpl[Int, Monad]): stream.ZStream[ZImpl[Int, Monad], Nothing, Int] =
//      stream.ZStream.accessStream(_.get.acc(a, b))
//
//
//    val live: Layer[Nothing, ZImpl[Int, Monad[Int]]] = ZLayer.succeed(Impl())
//
//  }
//
//  override def spec: ZSpec[TestEnvironment, Any] =
//    suite("ZStreams Typed With Tagless-Final")(
//      testM("acc")(
//        assertM(Impl.acc(2, 3).runCollect)(equalTo(Seq(5)))
//      )
//    ).provideLayer(Impl.live)
//
//}
