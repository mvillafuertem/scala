#!/usr/bin/env amm

import $ivy.`dev.zio::zio:1.0.0-RC18-2`
import zio._
import zio.console._
import zio.duration._
import zio.clock._

// amm `pwd`/ShelloWorld.sc
@doc("This explains what the function does")
@main
def main(): Unit = MyApp.main(Array())

object MyApp extends zio.App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic =
    for {
      _    <- putStrLn(s"Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Repeat every x seconds?")
      s    <- getStrLn
      _    <- putStrLn(s"Hello ${name}, welcome to ZIO!")
      _    <- putStrLn("")
      _    <- currentDateTime repeat Schedule.recurs(9) && Schedule
        .spaced(s.toIntOption.fold(1)(identity).second)
        .tapOutput(n => putStrLn(s"$n"))
        .tapInput(a => putStrLn(s"Completed $a"))
    } yield ()

}