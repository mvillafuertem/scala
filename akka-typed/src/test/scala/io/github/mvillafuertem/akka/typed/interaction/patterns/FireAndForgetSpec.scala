package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import org.scalatest.FlatSpecLike

/**
 * @author Miguel Villafuerte
 */
final class FireAndForgetSpec extends ScalaTestWithActorTestKit
  with FlatSpecLike {

  behavior of "Fire And Forget"

  it should "send printme" in {

    // g i v e n
    import FireAndForget._
    val requestActor = spawn(Printer.behavior)

    // w h e n
    requestActor ! PrintMe("Hello!")

    // t h e n

  }

}
