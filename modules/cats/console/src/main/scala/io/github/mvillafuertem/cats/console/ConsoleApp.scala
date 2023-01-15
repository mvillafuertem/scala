package io.github.mvillafuertem.cats.console

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.github.mvillafuertem.cats.console.instances.StdConsole

object ConsoleApp {

  def program[F[_]: Monad](implicit C: Console[F]): F[Unit] =
    for {
      _    <- C.putStrLn("Enter your name: ")
      line <- C.readLn
      _    <- C.putStrLn(s"Hello $line!")
    } yield ()

  def main(args: Array[String]): Unit =
    program(IO.asyncForIO, new StdConsole[IO]).unsafeRunSync()

}
