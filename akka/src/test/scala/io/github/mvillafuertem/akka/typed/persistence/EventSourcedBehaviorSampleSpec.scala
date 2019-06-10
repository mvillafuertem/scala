package io.github.mvillafuertem.akka.typed.persistence

import akka.actor.testkit.typed.Effect.Spawned
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import org.scalatest.{FlatSpec, Matchers}
import io.github.mvillafuertem.akka.typed.persistence.EventSourcedBehaviorSample._

class EventSourcedBehaviorSampleSpec extends FlatSpec with Matchers {

  behavior of "Event Sourced Behavior Sample Spec"

  it should "children with name" in {

    // G I V E N
    val testKit = BehaviorTestKit(EventSourcedBehaviorSample.behavior("id"))

    // W H E N
    testKit.run(Add("child"))

    // T H E N
    //testKit.expectEffect(Spawned(childActor, "child"))

  }
}
