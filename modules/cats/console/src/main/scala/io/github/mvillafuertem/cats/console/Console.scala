package io.github.mvillafuertem.cats.console

trait Console[F[_]] {

  def putStrLn(string: String): F[Unit]

  def readLn: F[String]

}
