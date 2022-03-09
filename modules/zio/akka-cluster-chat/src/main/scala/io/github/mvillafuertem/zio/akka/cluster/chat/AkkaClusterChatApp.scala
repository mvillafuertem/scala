package io.github.mvillafuertem.zio.akka.cluster.chat

import akka.actor.ActorSystem
import io.github.mvillafuertem.zio.akka.cluster.chat.application.Application._
import io.github.mvillafuertem.zio.akka.cluster.chat.configuration.Configuration._
import zio.console.Console
import zio.{ console, App, ExitCode, Has, ZIO }

// TODO
//  sbt zio/run -J-Dconfig.resource=application1.conf
//  sbt zio/run -J-Dconfig.resource=application2.conf
object AkkaClusterChatApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    program
      .provideLayer(Console.live ++ actorSystem)
      .catchAll(e => console.putStrLn(e.toString).orDie.as(ExitCode.failure))

  val program: ZIO[Console with Has[ActorSystem], Throwable, ExitCode] =
    (for {
      sharding <- startChatServer
      _        <- console.putStrLn("Hi! What's your name? (Type [exit] to stop)")
      name     <- console.getStrLn
      _        <- ZIO.when(name.toLowerCase == "exit" || name.trim.isEmpty)(ZIO.interrupt)
      _        <- console.putStrLn("Hi! Which chatroom do you want to join? (Type [exit] to stop)")
      room     <- console.getStrLn
      _        <- ZIO.when(room.toLowerCase == "exit" || room.trim.isEmpty)(ZIO.interrupt)
      _        <- joinChat(name, room, sharding)
    } yield ExitCode.success)

}
