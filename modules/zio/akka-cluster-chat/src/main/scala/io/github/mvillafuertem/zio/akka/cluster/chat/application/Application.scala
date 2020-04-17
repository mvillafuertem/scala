package io.github.mvillafuertem.zio.akka.cluster.chat.application

import akka.actor.ActorSystem
import io.github.mvillafuertem.zio.akka.cluster.chat.domain._
import zio.akka.cluster.pubsub.{PubSub, Publisher}
import zio.akka.cluster.sharding.{Entity, Sharding}
import zio.console.Console
import zio.{Has, IO, ZIO, console}

trait Application {

  def chatroomBehavior(pubSub: Publisher[String])(msg: ChatMessage): ZIO[Entity[List[String]], Nothing, Unit] =
    for {
      entity <- ZIO.environment[Entity[List[String]]]
      toSend <- msg match {
        case Message(name, m) => IO.succeed(s"$name: $m")
        case Join(name) =>
          entity.state
            .update(state => Some(name :: state.getOrElse(Nil)))
            .map(state => s"$name joined the room. There are now ${state} participant(s).")
        case Leave(name) =>
          entity.state
            .update(state => Some(state.getOrElse(Nil).filterNot(_ equalsIgnoreCase name)))
            .map(state => s"$name left the room. There are now ${state} participant(s).")
      }
      _ <- pubSub.publish(entity.id, toSend).ignore
    } yield ()

  def joinChat(
                name: String,
                room: String,
                sharding: Sharding[ChatMessage]
              ): ZIO[Console with Has[ActorSystem], Throwable, Unit] =
    for {
      pubSub   <- PubSub.createSubscriber[String]
      messages <- pubSub.listen(room)
      _        <- messages.take.flatMap(a => console.putStrLn(a)).forever.fork
      _        <- sharding.send(room, Join(name))
      _        <- chat(name, room, sharding)
    } yield ()

  def chat(name: String, room: String, sharding: Sharding[ChatMessage]): ZIO[Console, Throwable, Unit] =
    (for {
      msg <- console.getStrLn
      _   <- ZIO.when(msg.toLowerCase == "exit")(sharding.send(room, Leave(name)) *> ZIO.interrupt)
      _   <- ZIO.when(msg.trim.nonEmpty)(sharding.send(room, Message(name, msg)))
    } yield ()).forever

}
