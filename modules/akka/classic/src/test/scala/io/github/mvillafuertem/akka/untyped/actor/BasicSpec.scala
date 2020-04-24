package io.github.mvillafuertem.akka.untyped.actor

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike

/**
 * @author Miguel Villafuerte
 */
final class BasicSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with AnyFlatSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)

}
