package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

/**
  * @author Miguel Villafuerte
  */
object RequestResponse {

  case class Request(query: String, respondTo: ActorRef[Response])
  case class Response(result: String)

  object RequestActor {

    val behavior = Behaviors.receiveMessage[Request] {
      case Request(query, respondTo) =>
        respondTo ! Response(query)
        Behaviors.same
    }



  }

  object ResponseActor {

    val behavior = Behaviors.receiveMessage[Response] {
      case Response(result) =>
        println(s"Received message: $result")
        Behaviors.same
    }

  }
}


//object RequestResponse extends App {
//
//  import RequestActor._
//
//  val system = ActorSystem(RequestActor.behavior, "request")
//  val system2 = ActorSystem(ResponseActor.behavior, "response")
//
//  system ! Request("request", system2)
//
//}
