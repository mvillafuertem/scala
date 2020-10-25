package io.github.mvillafuertem.akka.typed.persistence

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import com.typesafe.config.{ Config, ConfigFactory }
import io.github.mvillafuertem.akka.typed.persistence.EventSourcedBehaviorSample._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class EventSourcedBehaviorSampleSpec
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorSampleSpec.conf)
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = testKit.shutdownTestKit()

  behavior of "Event Sourced Behavior Sample Spec"

  it should "add" in {

    // G I V E N
    val value = spawn(EventSourcedBehaviorSample.behavior("id"))
    val probe = TestProbe[State]()

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
    val probe = TestProbe[State]()

    // W H E N
    value ! Add("1")
    value ! Add("2")
    value ! Clear
    value ! GetValue(probe.ref)

    // T H E N
    probe.expectMessage(10 seconds, State(Nil))

  }
}

object EventSourcedBehaviorSampleSpec {

  def conf: Config = ConfigFactory.parseString(s"""
    akka.loglevel = INFO
    akka.loggers = [akka.testkit.TestEventListener]
    akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    akka.persistence.journal.inmem.test-serialization = on
    akka.actor.allow-java-serialization = on

    # duration to wait in expectMsg and friends outside of within() block
    # by default, will be dilated by the timefactor.
    akka.test.single-expect-default = 10s
    """)
}
