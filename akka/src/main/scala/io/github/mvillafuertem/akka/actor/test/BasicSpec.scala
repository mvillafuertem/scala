package io.github.mvillafuertem.akka.actor.test

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

final class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with FlatSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }



}
