package io.github.mvillafuertem.akka.untyped.fsm.vending.machine

import akka.actor.{ Actor, ActorLogging, ActorRef, Cancellable }
import io.github.mvillafuertem.akka.untyped.fsm.vending.machine.VendingMachineActor._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final class VendingMachineActor extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.dispatcher

  override def receive: Receive = idle

  def idle: Receive = {
    case Initialize(inventory, prices) => context.become(operational(inventory, prices))
    case _                             => sender() ! VendingError("MachineNotInitialized")
  }

  def operational(inventory: Map[String, Int], prices: Map[String, Int]): Receive = { case RequestProduct(product) =>
    inventory.get(product) match {

      case None | Some(0) =>
        sender() ! VendingError("ProductNotAvailable")

      case Some(_) =>
        val price = prices(product)
        sender() ! Instruction(s"Please insert $price dollars")
        context.become(waitForMoney(inventory, prices, product, 0, startReceiveMoneyTimeoutSchedule(), sender()))
    }
  }

  def waitForMoney(
    inventory: Map[String, Int],
    prices: Map[String, Int],
    product: String,
    money: Int,
    moneyTimeoutSchedule: Cancellable,
    requester: ActorRef
  ): Receive = {

    case ReceiveMoneyTimeout =>
      requester ! VendingError("RequestTimedOut")
      if (money > 0) requester ! GiveBackChange(money)
      context.become(operational(inventory, prices))

    case ReceiveMoney(amount) =>
      moneyTimeoutSchedule.cancel()
      val price = prices(product)
      if (money + amount >= price) {
        requester ! Deliver(product)

        if (money + amount - price > 0) requester ! GiveBackChange(money + amount - price)
        val newStock     = inventory(product) - 1
        val newInventory = inventory + (product -> newStock)
        context.become(operational(newInventory, prices))
      } else {
        val remainingMoney = price - money - amount
        requester ! Instruction(s"Please insert $remainingMoney dollars")
        context.become(waitForMoney(inventory, prices, product, money + amount, startReceiveMoneyTimeoutSchedule(), requester))
      }

  }

  def startReceiveMoneyTimeoutSchedule(): Cancellable =
    context.system.scheduler.scheduleOnce(1 second) {
      self ! ReceiveMoneyTimeout
    }

}

object VendingMachineActor {

  // M E S S A G E
  case class Deliver(product: String)
  case class GiveBackChange(amount: Int)
  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])
  case class Instruction(instruction: String)
  case class ReceiveMoney(amount: Int)
  case class RequestProduct(product: String)
  case class VendingError(reason: String)
  case object ReceiveMoneyTimeout

}
