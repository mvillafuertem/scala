package io.github.mvillafuertem.akka.untyped.fsm.vending.machine

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import io.github.mvillafuertem.akka.untyped.fsm.vending.machine.VendingMachineActor._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, OneInstancePerTest}

import scala.concurrent.duration._


class VendingMachineActorSpec extends TestKit(ActorSystem("VendingMachineActorSpec"))
  with ImplicitSender
  with FlatSpecLike
  with BeforeAndAfterAll
  with OneInstancePerTest {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  behavior of "A vending machine actor"

  it should "error when not initialized" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])

    // W H E N
    vendingMachine ! RequestProduct("coke")

    // T H E N
    expectMsg(VendingError("MachineNotInitialized"))

  }

  it should "report a product not available" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])
    vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 1))

    // W H E N
    vendingMachine ! RequestProduct("sandwich")

    // T H E N
    expectMsg(VendingError("ProductNotAvailable"))

  }

  it should "throw a timeout if I don't insert money" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])
    vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 1))

    // W H E N
    vendingMachine ! RequestProduct("coke")

    // T H E N
    expectMsg(Instruction("Please insert 1 dollars"))

    // A N D
    within(1.5 seconds) {
      expectMsg(VendingError("RequestTimedOut"))
    }

  }

  it should "handle the reception of partial money" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])
    vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
    vendingMachine ! RequestProduct("coke")
    expectMsg(Instruction("Please insert 3 dollars"))

    // W H E N
    vendingMachine ! ReceiveMoney(1)

    // T H E N
    expectMsg(Instruction("Please insert 2 dollars"))

    // A N D
    within(1.5 seconds) {
      expectMsg(VendingError("RequestTimedOut"))
    }

  }

  it should "deliver the product if I insert all money" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])
    vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
    vendingMachine ! RequestProduct("coke")
    expectMsg(Instruction("Please insert 3 dollars"))

    // W H E N
    vendingMachine ! ReceiveMoney(3)

    // T H E N
    expectMsg(Deliver("coke"))

  }

  it should "give back change and be able to request money for a new product" in {

    // G I V E N
    val vendingMachine = system.actorOf(Props[VendingMachineActor])
    vendingMachine ! Initialize(Map("coke" -> 10), Map("coke" -> 3))
    vendingMachine ! RequestProduct("coke")
    expectMsg(Instruction("Please insert 3 dollars"))

    // W H E N
    vendingMachine ! ReceiveMoney(4)

    // T H E N
    expectMsg(Deliver("coke"))
    expectMsg(GiveBackChange(1))

  }

}
