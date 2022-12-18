package io.github.mvillafuertem.cats.simple

import cats.effect.unsafe.implicits.global
import cats.effect.{ IO, Sync }
import cats.implicits._

import scala.io.StdIn

object SimpleApp {

  private [simple] def getUserName[F[_]: Sync]: F[String] =
    for {
      _    <- Sync[F].delay(println("What's your name?"))
      name <- Sync[F].delay(StdIn.readLine())
    } yield name

  private [simple] def greatUser[F[_]: Sync](name: String): F[Unit] =
    Sync[F].delay(println(s"Hello $name!"))

  private [simple] def program[F[_]: Sync]: F[String] = for {
    name <- getUserName
    _    <- greatUser(name)
  } yield s"Greeted $name"

  def main(args: Array[String]): Unit =
    program[IO].unsafeRunSync()

}
