package io.github.mvillafuertem.akka.stream.techniques

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object AdvancedBackpressure extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("AdvancedBackpressure")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()


}
