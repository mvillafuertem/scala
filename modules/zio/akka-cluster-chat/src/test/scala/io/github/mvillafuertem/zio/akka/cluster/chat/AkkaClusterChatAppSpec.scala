package io.github.mvillafuertem.zio.akka.cluster.chat

import zio.akka.cluster.pubsub.PubSub
import zio.console.Console
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestConsole, TestEnvironment }
import io.github.mvillafuertem.zio.akka.cluster.chat.configuration.actorSystem

object AkkaClusterChatAppSpec extends DefaultRunnableSpec {

  // g i v e n
  private val topic = "room1"
  private val user  = "Pepe"
  private val msg   = "hello"

  // w h e n
  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(testProgram)

  lazy val testProgram: ZSpec[Any with Console with TestConsole, Throwable] =
    testM("program") {
      assertM(
        for {
          _     <- TestConsole.feedLines(user, topic, msg)
          sub   <- PubSub.createSubscriber[String]
          queue <- sub.listen(topic)
          p     <- AkkaClusterChatApp.program.fork
          _     <- p.await
          out   <- TestConsole.output
          item  <- queue.take
          item1 <- queue.take
          _     <- p.interrupt
        } yield (out(0), out(1), item, item1)
        // t h e n
      )(
        equalTo(
          "Hi! What's your name? (Type [exit] to stop)\n",
          "Hi! Which chatroom do you want to join? (Type [exit] to stop)\n",
          s"$user joined the room. There are now () participant(s).",
          s"$user: $msg"
        )
      ).provideLayer(actorSystem ++ Console.live ++ TestConsole.any)
    }

}
