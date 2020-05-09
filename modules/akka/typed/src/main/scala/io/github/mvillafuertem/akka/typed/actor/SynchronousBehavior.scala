package io.github.mvillafuertem.akka.typed.actor

import akka.actor.typed._
import akka.actor.typed.scaladsl._

final class SynchronousBehavior {}

object SynchronousBehavior {

  sealed trait Cmd
  case object CreateAnonymousChild                 extends Cmd
  case class CreateChild(childName: String)        extends Cmd
  case class SayHelloToChild(childName: String)    extends Cmd
  case object SayHelloToAnonymousChild             extends Cmd
  case class SayHello(who: ActorRef[String])       extends Cmd
  case class LogAndSayHello(who: ActorRef[String]) extends Cmd

  val childActor = Behaviors.receiveMessage[String](_ => Behaviors.same[String])

  val myBehavior = Behaviors.receivePartial[Cmd] {
    case (context, CreateChild(name))          =>
      context.spawn(childActor, name)
      Behaviors.same
    case (context, CreateAnonymousChild)       =>
      context.spawnAnonymous(childActor)
      Behaviors.same
    case (context, SayHelloToChild(childName)) =>
      val child: ActorRef[String] = context.spawn(childActor, childName)
      child ! "hello"
      Behaviors.same
    case (context, SayHelloToAnonymousChild)   =>
      val child: ActorRef[String] = context.spawnAnonymous(childActor)
      child ! "hello stranger"
      Behaviors.same
    case (_, SayHello(who))                    =>
      who ! "hello"
      Behaviors.same
    case (context, LogAndSayHello(who))        =>
      context.log.info("Saying hello to {}", who.path.name)
      who ! "hello"
      Behaviors.same
  }

}
