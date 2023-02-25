package io.github.mvillafuertem.cats.console

import cats.data.{EitherT, StateT}
import cats.{Applicative, Id, MonadError}
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import cats.syntax.applicative._
import io.github.mvillafuertem.cats.console.ConsoleAppSpec.TestConsole
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import cats.syntax.option._

import java.util.UUID

final class ConsoleAppSpec extends AnyFlatSpecLike with Matchers {

  it should "program" in {

    val actual: IO[Seq[String]] = for {
      state  <- Ref.of[IO, Seq[String]](Seq.empty[String])
      _      <- ConsoleApp.program[IO](IO.asyncForIO, new TestConsole[IO](state))
      actual <- state.get
    } yield actual

    actual.unsafeRunSync() shouldBe Seq("Enter your name: ", "Hello test!")

  }

}

object ConsoleAppSpec {

  final class TestConsole[F[_]: Applicative](state: Ref[F, Seq[String]]) extends Console[F] {

    override def putStrLn(string: String): F[Unit] = state.update(_ :+ string)

    override def readLn: F[String] = "test".pure[F]

  }


  case class MyState(
                                           state: Map[String, String],
                                           uuid: Option[UUID],
                                           time: Option[Long]
                                         ) {

    def withMatch(
                   id: String,
                   response: String
                 ): MyState = copy(
      state + (id -> response)
    )

    def withUUID(uuid: UUID): MyState = copy(uuid = uuid.some)

    def withTime(time: Long): MyState = copy(time = time.some)


  }

  object MyState {
    val empty: MyState = MyState(
      Map.empty,
      None,
      None,
    )
  }


  type S[T] = StateT[Id, MyState, T]

  object S {
    def apply[T](
                  f: MyState => Id[(MyState, T)]
                ): S[T] = StateT[Id, MyState, T](f)
  }

  type MY[T] = EitherT[S, Throwable, T]

  object MY {
    def apply[T](
                  f: MyState => Id[(MyState, Either[Throwable, T])]
                ): MY[T] = EitherT[S, Throwable, T](S(f))
  }

  implicit val monadThrow: MonadError[MY, Throwable] = EitherT.catsDataMonadErrorForEitherT[S, Throwable]


  //  implicit val monadError: MonadError[S, Throwable] = new MonadThrow[S] {
  //    override def flatMap[A, B](fa: S[A])(f: A => S[B]): S[B] =
  //      fa.flatMap {
  //        case Left(value)  => raiseError(value)
  //        case Right(value) => f(value)
  //      }
  //
  //    override def tailRecM[A, B](a: A)(f: A => S[Either[A, B]]): S[B] = ???
  //
  //    override def raiseError[A](e: Throwable): S[A] =
  //      S[A](state => (state, Left(e)))
  //
  //    override def handleErrorWith[A](fa: S[A])(f: Throwable => S[A]): S[A] = fa.flatMap {
  //      case Left(value)  => f(value)
  //      case Right(value) => pure(value)
  //    }
  //
  //    override def pure[A](x: A): S[A] = S[A](state => (state, Right(x)))
  //  }




}
