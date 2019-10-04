package io.github.mvillafuertem.products

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.github.mvillafuertem.products.configuration.ProductsServiceConfiguration
import zio.DefaultRuntime

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object ProductsServiceApplication extends App
  with ProductsServiceConfiguration {

  // Z I O
  private val defaultRuntime: DefaultRuntime = new DefaultRuntime() {}
  private implicit val executionContext: ExecutionContext = defaultRuntime.Platform.executor.asEC

  // A K K A
  private implicit val actorSystem: ActorSystem = ActorSystem("products-service-application", defaultExecutionContext = Some(executionContext))
  private implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  actorSystem.log.info(s"Starting ${productsConfigurationProperties.name}... ${BuildInfo.toJson}")

  // R U N  A P P L I C A T I O N
  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(
      routes,
      productsConfigurationProperties.interface,
      productsConfigurationProperties.port)

  serverBinding.onComplete {
    case Success(bound) =>
      actorSystem.log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")

    case Failure(e) =>
      actorSystem.log.error(e, s"Server could not start with parameters [host:port]=[${productsConfigurationProperties.interface},${productsConfigurationProperties.port}]")
      actorSystem.terminate()
  }

  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
