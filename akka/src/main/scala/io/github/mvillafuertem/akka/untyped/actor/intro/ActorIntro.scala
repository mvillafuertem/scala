package io.github.mvillafuertem.akka.untyped.actor.intro

import akka.actor.{Actor, ActorSystem, Props}

object ActorIntro extends App {

  // Declare ActorSystem
  val actorSystem: ActorSystem = ActorSystem("ActorIntro")
  println(actorSystem.name)

  // Declare Actor
  class WordCountActor extends Actor {

    var totalWords = 0

    // Behavior
    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word count] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }

  }

  // Instantiate Actor
  val wordCounterActor = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounterActor = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // Communicate Asynchronous
  wordCounterActor ! "I am a message"
  anotherWordCounterActor ! "I am another message"

  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  val pepe = actorSystem.actorOf(Props(new Person("Pepe")))
  val alice = actorSystem.actorOf(Person.props("Alice"))

  pepe ! "hi"
  alice ! "hi"

}
