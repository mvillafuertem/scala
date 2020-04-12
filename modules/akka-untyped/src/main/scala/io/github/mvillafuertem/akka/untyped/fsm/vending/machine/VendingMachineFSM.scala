package io.github.mvillafuertem.akka.untyped.fsm.vending.machine

import akka.actor.{ActorRef, FSM}
import io.github.mvillafuertem.akka.untyped.fsm.vending.machine.VendingMachineFSM._

import scala.concurrent.duration._

final class VendingMachineFSM extends FSM[VendingState, VendingData] {


  startWith(Idle, Uninitialized)

  when(Idle) {

    case Event(Initialize(inventory, prices), Uninitialized) =>
      // equivalent with context.become(operational(inventory, prices))
      goto(Operational) using Initialized(inventory, prices)
    case _ => sender() ! VendingError("MachineNotInitialized")
      stay()

  }

  when(Operational) {
    case Event(RequestProduct(product), Initialized(inventory, prices)) => inventory.get(product) match {

      case None | Some(0) =>
        sender() ! VendingError("ProductNotAvailable")
        stay()
      case Some(_) =>
        val price = prices(product)
        sender() ! Instruction(s"Please insert $price dollars")
        //context.become(waitForMoney(inventory, prices, product, 0, startReceiveMoneyTimeoutSchedule(), sender()))
        goto(WaitForMoney) using WaitForMoneyData(inventory, prices, product, 0, sender())
    }
  }

  when(WaitForMoney, stateTimeout = 1 second) {
    case Event(StateTimeout, WaitForMoneyData(inventory, prices, product, money, requester)) =>
      requester ! VendingError("RequestTimedOut")
      if (money > 0) requester ! GiveBackChange(money)
      //context.become(operational(inventory, prices))
      goto(Operational) using Initialized(inventory, prices)
    case Event(ReceiveMoney(amount), WaitForMoneyData(inventory, prices, product, money, requester)) =>
      val price = prices(product)
      if (money + amount >= price) {
        requester ! Deliver(product)

        if (money + amount - price > 0) requester ! GiveBackChange(money + amount - price)
        val newStock = inventory(product) - 1
        val newInventory = inventory + (product -> newStock)
        // context.become(operational(newInventory, prices))
        goto(Operational) using Initialized(newInventory, prices)
      } else {
        val remainingMoney = price - money - amount
        requester ! Instruction(s"Please insert $remainingMoney dollars")
        // context.become(waitForMoney(
        //   inventory, prices, product,
        //   money + amount,
        //   startReceiveMoneyTimeoutSchedule(), requester))
        stay() using WaitForMoneyData(inventory, prices, product, money + amount, requester)
      }
  }

  onTransition {
    case stateA -> stateB => log.info(s"Trasitioning from $stateA to $stateB")
  }

  initialize()

}

object VendingMachineFSM {

  // M E S S A G E
  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])
  case class RequestProduct(product: String)
  case class Instruction(instruction: String)
  case class ReceiveMoney(amount: Int)
  case class Deliver(product: String)
  case class GiveBackChange(amount: Int)
  case class VendingError(reason: String)

  // S T A T E
  trait VendingState
  case object Idle extends VendingState
  case object Operational extends VendingState
  case object WaitForMoney extends VendingState

  // D A T A
  trait VendingData
  case object Uninitialized extends VendingData
  case class Initialized(inventory: Map[String, Int], prices: Map[String, Int]) extends VendingData
  case class WaitForMoneyData(inventory: Map[String, Int],
                              prices: Map[String, Int],
                              product: String,
                              money: Int,
                              requester: ActorRef) extends VendingData
}
