package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

/**
 * @author Miguel Villafuerte
 */
final class RequestResponseWithAskSpec extends ScalaTestWithActorTestKit
  with AsyncFlatSpecLike
  with Matchers {


  behavior of "Request Response With Ask"

  it should "ask and get response" in {

    // g i v e n
    import RequestResponseWithAsk._
    import akka.actor.typed.scaladsl.AskPattern._
    val value = spawn(RequestResponseWithAsk.behavior)

    // w h e n
    val future: Future[Response] = value ? (ref => Request("request", ref))

    // t h e n
    future.map { actual =>
      actual shouldBe Response("Hello!")
    }

  }

}
