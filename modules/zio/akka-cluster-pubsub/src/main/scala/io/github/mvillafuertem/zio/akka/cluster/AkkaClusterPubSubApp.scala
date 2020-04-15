package io.github.mvillafuertem.zio.akka.cluster

import io.github.mvillafuertem.zio.akka.cluster.application._
import io.github.mvillafuertem.zio.akka.cluster.configuration._
import zio._
import zio.console.Console


// TODO
//  sbt zio/run -J-Dconfig.resource=application1.conf
//  sbt zio/run -J-Dconfig.resource=application2.conf
object AkkaClusterPubSubApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = program

  val program: ZIO[Console, Nothing, Int] =
    (for {
      sharding <- startChatServer
      _ <- console.putStrLn("Hi! What's your name? (Type [exit] to stop)")
      name <- console.getStrLn
      _ <- ZIO.when(name.toLowerCase == "exit" || name.trim.isEmpty)(ZIO.interrupt)
      _ <- console.putStrLn("Hi! Which chatroom do you want to join? (Type [exit] to stop)")
      room <- console.getStrLn
      _ <- ZIO.when(room.toLowerCase == "exit" || room.trim.isEmpty)(ZIO.interrupt)
      _ <- joinChat(name, room, sharding)
    } yield 0)
      .provideLayer(Console.live ++ actorSystem)
      .catchAll(e => console.putStrLn(e.toString).as(1))

}
