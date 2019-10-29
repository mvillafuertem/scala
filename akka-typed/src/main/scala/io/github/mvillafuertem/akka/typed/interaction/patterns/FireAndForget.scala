package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.typed.scaladsl.Behaviors

/**
  * @author Miguel Villafuerte
  */
object FireAndForget {

  case class PrintMe(message: String)

  object Printer {
    val behavior = Behaviors.receive[PrintMe] {
      case (context, PrintMe(message)) =>
        context.log.info(message)
        Behaviors.same
    }
  }

}
//object FireAndForget extends App {
//
//  import Printer._
//
//  val system = ActorSystem(behavior, "fire-and-forget")
//  val printer: ActorRef[PrintMe] = system
//  printer ! PrintMe("Hello! 1")
//  printer ! PrintMe("Hello! 2")
//
//}
