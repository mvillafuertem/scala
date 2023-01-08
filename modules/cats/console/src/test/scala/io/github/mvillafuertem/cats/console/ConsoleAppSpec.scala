package io.github.mvillafuertem.cats.console

import cats.Applicative
import cats.effect.unsafe.implicits.global
import cats.effect.{ IO, Ref }
import cats.syntax.applicative._
import io.github.mvillafuertem.cats.console.ConsoleAppSpec.TestConsole
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

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

}
