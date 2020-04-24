package io.github.mvillafuertem.akka.untyped.actor.intro

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      // Actor can be response to another actor
      case "Hi!"           => context.sender() ! "Hello, there!"
      case message: String =>
        // Actors have information about their context and about themselves
        // context.self === `this` in OOP
        println(s"[${context.self.path}]")
        println(s"[${self.path}]")
        println(s"[simple actor] I have received $message")
      case number: Int              => println(s"[simple actor] I have received a number $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received siomething special $contents")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref)                       => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "[forwarding]")
    }
  }

  val actorSystem: ActorSystem = ActorSystem("ActorCapabilities")
  val simpleActor: ActorRef    = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  case class SpecialMessage(contents: String)

  // The messages can be of any type
  // - Messages must be Immutable
  // - Messages must be Serializable
  simpleActor ! "hello, actor"
  simpleActor ! 42
  simpleActor ! SpecialMessage("some special content")

  // Use of self
  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor a I am proud of it")

  // Actors can be reply to messages
  val pepe   = actorSystem.actorOf(Props[SimpleActor], "pepe")
  val alicia = actorSystem.actorOf(Props[SimpleActor], "alicia")

  case class SayHiTo(actorRef: ActorRef)
  pepe ! SayHiTo(alicia)

  // Dead Letters
  pepe ! "Hi!"

  // Forwarding messages
  // D -> A -> B
  case class WirelessPhoneMessage(content: String, actorRef: ActorRef)
  pepe ! WirelessPhoneMessage("Hi", alicia)

  // Exercise
  //
  // A CounterActor
  // - Increment
  // - Decrement
  // - Print
  //
  // A BankAccountActor
  // - Deposit an amount
  // - Withdraw an amount
  // - Statement
  // replies with
  // - Success
  // - Failure
  //
  // interact with some other kind actor
}
