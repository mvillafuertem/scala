package io.github.mvillafuertem.zio.akka.cluster

import _root_.akka.actor.ActorSystem
import zio._
import zio.akka.cluster.pubsub.{PubSub, Publisher}
import zio.akka.cluster.sharding.{Entity, Sharding}
import zio.console.Console


// TODO
//  sbt zio/run -J-Dconfig.resource=application1.conf
//  sbt zio/run -J-Dconfig.resource=application2.conf
object AkkaClusterApp extends App {

  sealed trait ChatMessage
  case class Message(name: String, msg: String) extends ChatMessage
  case class Join(name: String) extends ChatMessage
  case class Leave(name: String) extends ChatMessage


  val actorSystem: ZLayer[Any, Throwable, Has[ActorSystem]] =
    ZLayer.fromManaged(Managed.make(Task(ActorSystem("Chat")))(sys => Task.fromFuture(_ => sys.terminate()).either))

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = program


  val program: zio.ZIO[zio.Has[zio.console.Console.Service],Nothing,Int] =
            (for {
              sharding <- startChatServer.provideLayer(actorSystem)
              _ <- console.putStrLn("Hi! What's your name? (Type [exit] to stop)")
              name <- console.getStrLn
              _ <- ZIO.when(name.toLowerCase == "exit" || name.trim.isEmpty)(ZIO.interrupt)
              _ <- console.putStrLn("Hi! Which chatroom do you want to join? (Type [exit] to stop)")
              room <- console.getStrLn
              _ <- ZIO.when(room.toLowerCase == "exit" || room.trim.isEmpty)(ZIO.interrupt)
              _ <- joinChat(name, room, sharding)
            } yield 0).provideLayer(Console.live).catchAll(e => console.putStrLn(e.toString).as(1))


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
            .update(state => Some(state.getOrElse(Nil).filterNot(_ == name)))
            .map(state => s"$name left the room. There are now ${state} participant(s).")
      }
      _ <- pubSub.publish(entity.id, toSend).ignore
    } yield ()

  val startChatServer: ZIO[Has[ActorSystem], Throwable, Sharding[ChatMessage]] =
    for {
      pubSub <- PubSub.createPublisher[String]
      sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
    } yield sharding

  def joinChat(
                name: String,
                room: String,
                sharding: Sharding[ChatMessage]
              ): ZIO[Console, Throwable, Unit] =
    for {
      pubSub <- PubSub.createSubscriber[String].provideLayer(actorSystem)
      messages <- pubSub.listen(room)
      _ <- messages.take.flatMap(console.putStrLn).forever.fork
      _ <- sharding.send(room, Join(name))
      _ <- chat(name, room, sharding)
    } yield ()

  def chat(name: String, room: String, sharding: Sharding[ChatMessage]): ZIO[Console, Throwable, Unit] =
    (for {
      msg <- console.getStrLn
      _ <- ZIO.when(msg.toLowerCase == "exit")(sharding.send(room, Leave(name)) *> ZIO.interrupt)
      _ <- ZIO.when(msg.trim.nonEmpty)(sharding.send(room, Message(name, msg)))
    } yield ()).forever

}
