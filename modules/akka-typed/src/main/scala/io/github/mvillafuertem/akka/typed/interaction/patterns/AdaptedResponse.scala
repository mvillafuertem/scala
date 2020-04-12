package io.github.mvillafuertem.akka.typed.interaction.patterns

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

/**
  * @author Miguel Villafuerte
  */
object AdaptedResponse {

  case class Request(query: String, respondTo: ActorRef[WrappedResponse])
  case class Response(result: String)

  object ActorRequester {

    val behavior = Behaviors.receiveMessage[Request] {
      case Request(query, respondTo) =>
        respondTo ! WrappedResponse(Response(query))
        Behaviors.same
    }

  }

  case class WrappedResponse(response: Response)

  object ActorReceiver {

    val behavior = Behaviors.setup[WrappedResponse] { context =>
      val responseAdapter =
        context.messageAdapter(response => WrappedResponse(response))

      Behaviors.receiveMessage[WrappedResponse] {
        case wrappedResponse: WrappedResponse =>
          println(s"Here is your response: ${wrappedResponse.response}")
          Behaviors.same
      }
    }

  }

}
