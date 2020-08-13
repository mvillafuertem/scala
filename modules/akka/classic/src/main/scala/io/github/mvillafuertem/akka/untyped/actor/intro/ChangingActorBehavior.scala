package io.github.mvillafuertem.akka.untyped.actor.intro

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import io.github.mvillafuertem.akka.untyped.actor.intro.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD   = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state: String = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(message)    =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(actorRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) =>
      case Ask(_)          => sender() ! KidAccept
    }
    def sadReceive: Receive   = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Ask(_)          => sender() ! KidReject
    }
  }

  class Mom extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want play?")
      case KidAccept        => println("Yay, my kid is happy!")
      case KidReject        => println("My kid is sad, but as he's healthy")
    }
  }

  val system                      = ActorSystem("ChangingActorBehavior")
  val fussyKid: ActorRef          = system.actorOf(Props[FussyKid]())
  val statelessFussyKid: ActorRef = system.actorOf(Props[StatelessFussyKid]())
  val mom: ActorRef               = system.actorOf(Props[Mom]())

  mom ! MomStart(statelessFussyKid)

}
