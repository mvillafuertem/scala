package io.github.mvillafuertem.akka.typed.actor

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import io.github.mvillafuertem.akka.typed.actor.AsynchronousBehavior._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class AsynchronousBehaviorSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val testKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  behavior of "Asynchronous Behavior Spec"

  it should "actor with name" in {

    // G I V E N
    val pinger = testKit.spawn(echoActor, "ping")
    val probe = testKit.createTestProbe[Pong]()

    // W H E N
    pinger ! Ping("hello", probe.ref)

    // T H E N
    probe.expectMessage(Pong("hello"))

  }

  it should "actor anonymous" in {

    // G I V E N
    val pinger = testKit.spawn(echoActor)
    val probe = testKit.createTestProbe[Pong]()

    // W H E N
    pinger ! Ping("hello", probe.ref)

    // T H E N
    probe.expectMessage(Pong("hello"))

  }

  it should "stopping actor" in {

    // G I V E N
    val pinger1 = testKit.spawn(echoActor, "pinger")
    val probe = testKit.createTestProbe[Pong]()

    // W H E N
    pinger1 ! Ping("hello", probe.ref)

    // T H E N
    probe.expectMessage(Pong("hello"))
    testKit.stop(pinger1)

    // A N D

    // G I V E N
    val pinger2 = testKit.spawn(echoActor, "pinger")

    // W H E N
    pinger2 ! Ping("hello", probe.ref)

    // T H E N
    probe.expectMessage(Pong("hello"))
    testKit.stop(pinger2, 10 seconds)

  }

}