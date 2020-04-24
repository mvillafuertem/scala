package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

/**
 * @author Miguel Villafuerte
 */
object RequestResponseWithAsk {

  val behavior = Behaviors.receiveMessage[Request] {
    case Request(query, respondTo) =>
      respondTo ! Response("Hello!")
      Behaviors.same
  }

  case class Request(query: String, respondTo: ActorRef[Response])
  case class Response(result: String)
  //  import ActorRequester._
//
//  val system = ActorSystem(behavior, "request-and-response-with-ask")
//
//  implicit val scheduler = system.scheduler
//  implicit val timeout = Timeout(5 seconds)
//  implicit val ec = system.executionContext
//
//  val future: Future[Response] = system ? (ref => Request("request", ref))
//
//  future.onComplete {
//    case Failure(_)     => println("something bad happened")
//    case Success(value) => println(value.result)
//  }

}
