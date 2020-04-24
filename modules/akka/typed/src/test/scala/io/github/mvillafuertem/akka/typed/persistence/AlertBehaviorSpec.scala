package io.github.mvillafuertem.akka.typed.persistence

import java.util.Date

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import com.typesafe.config.{ Config, ConfigFactory }
import io.github.mvillafuertem.akka.typed.persistence.AlertBehavior._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterAll, OneInstancePerTest }

final class AlertBehaviorSpec
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorSampleSpec.conf)
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with OneInstancePerTest {

  override def afterAll(): Unit = testKit.shutdownTestKit()

  behavior of "Alert Behavior Spec"

  it should "open" in {

    // G I V E N
    val value = spawn(AlertBehavior.behavior("id"))
    val probe = TestProbe[State]
    val alert = Alert(1234567890L, "ALERT", "Alert with id 1234567890", new Date().toInstant.toEpochMilli, true)

    // W H E N
    value ! Open(alert)
    value ! GetAlert(probe.ref)

    // T H E N
    probe.expectMessage(State(List(alert)))

  }

  it should "close" in {

    // G I V E N
    val value = spawn(AlertBehavior.behavior("id"))
    val probe = TestProbe[State]
    val alert = Alert(1234567890L, "ALERT", "Alert with id 1234567890", new Date().toInstant.toEpochMilli, true)

    // W H E N
    value ! Open(alert)
    value ! Close(1234567890L)
    value ! GetAlert(probe.ref)

    // T H E N
    probe.expectMessage(State(Nil))

  }

}

object AlertBehaviorSpec {
  def conf: Config = ConfigFactory.parseString(s"""
    akka.loglevel = INFO
    akka.loggers = [akka.testkit.TestEventListener]
    akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    akka.persistence.journal.inmem.test-serialization = on
    akka.actor.allow-java-serialization = on
    """)
}
