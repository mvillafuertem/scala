package io.github.mvillafuertem.zio.akka.cluster.chat.configuration

import akka.actor.ActorSystem
import io.github.mvillafuertem.zio.akka.cluster.chat.application.Application.chatroomBehavior
import io.github.mvillafuertem.zio.akka.cluster.chat.domain.Domain._
import zio.akka.cluster.pubsub.PubSub
import zio.akka.cluster.sharding.Sharding
import zio.{ Has, Managed, Task, ZIO, ZLayer }

trait Configuration {

  val actorSystem: ZLayer[Any, Throwable, Has[ActorSystem]] =
    ZLayer.fromManaged(Managed.make(Task(ActorSystem("Chat")))(sys => Task.fromFuture(_ => sys.terminate()).either))

  val startChatServer: ZIO[Has[ActorSystem], Throwable, Sharding[ChatMessage]] =
    for {
      pubSub   <- PubSub.createPublisher[String]
      sharding <- Sharding.start("Chat", chatroomBehavior(pubSub))
    } yield sharding

}

object Configuration extends Configuration
