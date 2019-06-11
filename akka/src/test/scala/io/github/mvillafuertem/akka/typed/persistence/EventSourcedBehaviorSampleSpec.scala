package io.github.mvillafuertem.akka.typed.persistence

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.akka.typed.persistence.EventSourcedBehaviorSample._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class EventSourcedBehaviorSampleSpec extends ScalaTestWithActorTestKit(EventSourcedBehaviorSampleSpec.conf)
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = testKit.shutdownTestKit()

  behavior of "Event Sourced Behavior Sample Spec"

  it should "add" in {

    // G I V E N
    val value = spawn(EventSourcedBehaviorSample.behavior("id"))
    val probe = TestProbe[State]

    // W H E N
    value ! Add("1")
    value ! Add("2")
    value ! GetValue(probe.ref)

    // T H E N
    probe.expectMessage(State(List("2", "1")))

  }

  it should "clear" in {

    // G I V E N
    val value = spawn(EventSourcedBehaviorSample.behavior("id"))
    val probe = TestProbe[State]

    // W H E N
    value ! Add("1")
    value ! Add("2")
    value ! Clear
    value ! GetValue(probe.ref)

    // T H E N
    probe.expectMessage(State(Nil))

  }
}

object EventSourcedBehaviorSampleSpec {


  def conf: Config = ConfigFactory.parseString(s"""
    akka.loglevel = INFO
    akka.loggers = [akka.testkit.TestEventListener]
    akka.persistence.journal.plugin = "inmemory-journal"
    akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
    """)
}
