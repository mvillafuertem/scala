package io.github.mvillafuertem.products

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import zio.DefaultRuntime

import scala.concurrent.ExecutionContext

object ProductsServiceApplication extends App {

  // Z I O
  private val defaultRuntime: DefaultRuntime = new DefaultRuntime() {}
  private implicit val executionContext: ExecutionContext = defaultRuntime.Platform.executor.asEC

  // A K K A
  private implicit val actorSystem: ActorSystem = ActorSystem("products-service-application", defaultExecutionContext = Some(executionContext))
  private implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()


}
