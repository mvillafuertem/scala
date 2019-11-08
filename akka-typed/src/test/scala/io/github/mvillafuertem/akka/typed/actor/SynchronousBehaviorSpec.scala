package io.github.mvillafuertem.akka.typed.actor

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.{Spawned, SpawnedAnonymous}
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import io.github.mvillafuertem.akka.typed.actor.SynchronousBehavior._
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.event.Level

class SynchronousBehaviorSpec extends FlatSpec with Matchers {

  behavior of "Synchronous Behavior Spec"

  it should "children with name" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)

    // W H E N
    testKit.run(CreateChild("child"))

    // T H E N
    testKit.expectEffect(Spawned(childActor, "child"))

  }

  it should "children anonymous" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)

    // W H E N
    testKit.run(CreateAnonymousChild)

    // T H E N
    testKit.expectEffect(SpawnedAnonymous(childActor))

  }

  it should "sending messages" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)
    val inbox = TestInbox[String]()

    // W H E N
    testKit.run(SayHello(inbox.ref))

    // T H E N
    inbox.expectMessage("hello")

  }

  it should "sending a message to a child actor" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)

    // W H E N
    testKit.run(SayHelloToChild("child"))

    // T H E N
    val childInbox = testKit.childInbox[String]("child")
    childInbox.expectMessage("hello")

  }

  it should "sending a message to anonymous children" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)

    // W H E N
    testKit.run(SayHelloToAnonymousChild)

    // T H E N
    val child = testKit.expectEffectType[SpawnedAnonymous[String]]
    val childInbox = testKit.childInbox(child.ref)
    childInbox.expectMessage("hello stranger")

  }

  it should "checking for Log Messages" in {

    // G I V E N
    val testKit = BehaviorTestKit(myBehavior)
    val inbox = TestInbox[String]("Inboxer")

    // W H E N
    testKit.run(LogAndSayHello(inbox.ref))

    // T H E N
    testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "Saying hello to Inboxer"))

  }

}
