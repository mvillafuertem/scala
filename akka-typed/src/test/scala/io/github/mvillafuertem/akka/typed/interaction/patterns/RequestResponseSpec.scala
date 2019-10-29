package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import org.scalatest.FlatSpecLike

/**
 * @author Miguel Villafuerte
 */
final class RequestResponseSpec extends ScalaTestWithActorTestKit
  with FlatSpecLike {

  behavior of "Request Response"

  it should "seand a request and get a response" in {

    // g i v e n
    import RequestResponse._
    val requestActor = spawn(RequestActor.behavior)
    val probeResponseActor = TestProbe[Response]

    // w h e n
    requestActor ! Request("Hello!", probeResponseActor.ref)

    // t h e n
    probeResponseActor.expectMessage(Response("Hello!"))

  }

}
