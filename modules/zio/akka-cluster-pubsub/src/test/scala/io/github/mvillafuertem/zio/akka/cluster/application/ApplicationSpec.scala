package io.github.mvillafuertem.zio.akka.cluster.application

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.zio.akka.cluster.domain._
import zio._
import zio.akka.cluster.pubsub.PubSub
import zio.akka.cluster.sharding.Sharding
import zio.console.Console
import zio.test.Assertion._
import zio.test._
import zio.test.environment.{TestConsole, TestEnvironment}


object ApplicationSpec extends DefaultRunnableSpec {

  // g i v e n
  private val config: Config = ConfigFactory
    .load("application-test")
    .getConfig("application")
  private val actorSystem: TaskLayer[Has[ActorSystem]] =
    ZLayer.fromManaged(Managed.make(
      Task(ActorSystem("Test", config)))(sys =>
      Task.fromFuture(_ => sys.terminate()).either))

  private val topic = "room1"
  private val user = "Pepe"
  private val msg = "hello"
  private val message: ChatMessage = Message(user, msg)
  private val join: ChatMessage = Join(user)
  private val leave: ChatMessage = Leave(user)

  // w h e n
  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      testBehaviorWhenReceivedSendMessage,
      testBehaviorWhenReceivedJoinMessage,
      testBehaviorWhenReceivedLeaveMessage,
      testChatWithSomeBody,
      testJoinToTheChat,
      testExitToTheChat
    )

  lazy val testBehaviorWhenReceivedSendMessage: ZSpec[Any, Throwable] =
    testM("behavior when received send message") {
      assertM(
        for {
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          _ <- sharding.send(topic, message)
          item <- queue.take
        } yield item
        // t h e n
      )(equalTo(user + ": " + msg)).provideLayer(actorSystem)
    }

  lazy val testBehaviorWhenReceivedJoinMessage: ZSpec[Any, Throwable] =
    testM("behavior when received join message") {
      assertM(
        for {
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          _ <- sharding.send(topic, join)
          item <- queue.take
        } yield item
        // t h e n
      )(equalTo(s"$user joined the room. There are now () participant(s)."))
        .provideLayer(actorSystem)
    }

  lazy val testBehaviorWhenReceivedLeaveMessage: ZSpec[Any, Throwable] =
    testM("behavior when received leave message") {
      assertM(
        for {
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          _ <- sharding.send(topic, leave)
          item <- queue.take
        } yield item
        // t h e n
      )(equalTo(s"$user left the room. There are now () participant(s)."))
        .provideLayer(actorSystem)
    }

  lazy val testChatWithSomeBody: ZSpec[Any with Console with TestConsole, Throwable] =
    testM("chat with somebody") {
      assertM(
        for {
          _ <- TestConsole.feedLines(msg)
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          chat <- chat(user, topic, sharding).fork
          item <- queue.take
          _ <- chat.interrupt
        } yield item
        // t h e n
      )(equalTo(s"$user: $msg"))
        .provideLayer(actorSystem ++ TestConsole.any)
    }

  lazy val testJoinToTheChat: ZSpec[Any with Console with TestConsole, Throwable] =
    testM("join to the chat") {
      assertM(
        for {
          _ <- TestConsole.feedLines(msg)
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          joinChat <- joinChat(user, topic, sharding).fork
          item <- queue.take
          _ <- joinChat.interrupt
        } yield item
        // t h e n
      )(equalTo(s"$user joined the room. There are now () participant(s)."))
        .provideLayer(actorSystem ++ TestConsole.any)
    }

  lazy val testExitToTheChat: ZSpec[Any with Console with TestConsole, Throwable] =
    testM("exit to the chat") {
      assertM(
        for {
          interrupted <- Ref.make(false)
          _ <- TestConsole.feedLines("exit")
          pubSub <- PubSub.createPubSub[String]
          queue <- pubSub.listen(topic)
          sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
          chat <- chat(user, topic, sharding).onInterrupt(interrupted.set(true)).fork
          _    <- chat.await
          item <- queue.take
          i <- interrupted.get
        } yield (i, item)
        // t h e n
      )(equalTo(true, s"$user left the room. There are now () participant(s)."))
        .provideLayer(actorSystem ++ TestConsole.any)
    }

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential))

}
