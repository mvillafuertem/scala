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
  private val message: ChatMessage = Message("Pepe", "hello")
  private val join: ChatMessage = Join("Pepe")
  private val leave: ChatMessage = Leave("Pepe")

  val value: ZSpec[Any, Throwable] = testM("behavior when received send message") {
    assertM(
      for {
        pubSub <- PubSub.createPubSub[String]
        queue <- pubSub.listen(topic)
        sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
        _ <- sharding.send(topic, message)
        item <- queue.take
      } yield item
      // t h e n
    )(equalTo("Pepe: hello")).provideLayer(actorSystem)
  }

  val value1: ZSpec[Any, Throwable] = testM("behavior when received join message") {
    assertM(
      for {
        pubSub <- PubSub.createPubSub[String]
        queue <- pubSub.listen(topic)
        sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
        _ <- sharding.send(topic, join)
        item <- queue.take
      } yield item
      // t h e n
    )(equalTo("Pepe joined the room. There are now () participant(s)."))
      .provideLayer(actorSystem)
  }

  val value2: ZSpec[Any, Throwable] = testM("behavior when received leave message") {
    assertM(
      for {
        pubSub <- PubSub.createPubSub[String]
        queue <- pubSub.listen(topic)
        sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
        _ <- sharding.send(topic, leave)
        item <- queue.take
      } yield item
      // t h e n
    )(equalTo("Pepe left the room. There are now () participant(s)."))
      .provideLayer(actorSystem)
  }

  val value3: ZSpec[Any with Console with TestConsole, Throwable] = testM("join chat") {
    assertM(
      for {
        _ <- TestConsole.feedLines("exit")
        pubSub <- PubSub.createPubSub[String]
        sharding <- Sharding.start[ChatMessage, List[String]]("Chat", chatroomBehavior(pubSub))
        a <- chat("Pepe", topic, sharding)
        questionVector <- TestConsole.output
      } yield questionVector(0)
      // t h e n
    )(equalTo(""))
      .provideLayer(actorSystem ++ TestConsole.any)
  }

  val value4: ZSpec[Any with Console with TestConsole, Any] = testM("my program") {
    assertM(
      for {
        _ <- TestConsole.feedLines("Hola Pepe")
        _ <- program()
        questionVector <- TestConsole.output
      } yield questionVector(0)
      // t h e n
    )(equalTo("New message: Hola Pepe\n"))
      .provideLayer(TestConsole.any)
  }

  // w h e n
  override def spec: ZSpec[TestEnvironment, Any] = suite(getClass.getSimpleName)(value,
    value1,
    value2,
    //value3,
    value4,
  )

  override def aspects: List[TestAspect[Nothing, TestEnvironment, Nothing, Any]] =
    List(TestAspect.executionStrategy(ExecutionStrategy.Sequential))

}
