package io.github.mvillafuertem.cats.console.instances

import cats.effect.Sync
import io.github.mvillafuertem.cats.console.Console

import scala.io.StdIn

final class StdConsole[F[_]: Sync] extends Console[F] {

  override def putStrLn(string: String): F[Unit] =
    Sync[F].delay(println(string))

  override def readLn: F[String] =
    Sync[F].delay(StdIn.readLine())

}
