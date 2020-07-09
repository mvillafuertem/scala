package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import org.scalatest.flatspec.AnyFlatSpecLike

/**
 * @author Miguel Villafuerte
 */
final class AdaptedResponseSpec extends ScalaTestWithActorTestKit with AnyFlatSpecLike {

  behavior of "Adapted Response"

  it should "send a request and get a wrapped response" in {

    // g i v e n
    import AdaptedResponse._
    val actorRequester     = spawn(ActorRequester.behavior)
    val probeResponseActor = TestProbe[WrappedResponse]()

    // w h e n
    actorRequester ! Request("Hello!", probeResponseActor.ref)

    // t h e n
    probeResponseActor.expectMessage(WrappedResponse(Response("Hello!")))

  }

}
