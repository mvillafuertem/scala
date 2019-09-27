package io.github.mvillafuertem.zio

import zio.console._
import zio.{ZIO, console}

object ZioApplication {
  def sayHello: ZIO[Console, Nothing, Unit] =
    console.putStrLn("Hello, World!")
}
