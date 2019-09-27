package io.github.mvillafuertem.akka.untyped.actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

/**
 * @author Miguel Villafuerte
 */
final class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with FlatSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }



}