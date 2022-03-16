package io.github.mvillafuertem.tapir.configuration

import akka.actor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation._enhanceRouteWithConcatenation
import io.github.mvillafuertem.tapir.api.{ ProductsApi, SwaggerApi }
import io.github.mvillafuertem.tapir.configuration.ActorSystemConfiguration.ZActorSystem
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import io.github.mvillafuertem.tapir.infrastructure.SlickProductsRepository
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.{ Runtime, UIO, ZEnv, ZEnvironment, ZIO, ZLayer, ZManaged }

import scala.concurrent.Future
import scala.concurrent.duration._

trait AkkaHttpServerConfiguration {

  val live: ZLayer[ZEnv with ZActorSystem with ZProductsConfigurationProperties, Throwable, Future[Http.ServerBinding]] =
    ZLayer.fromManagedEnvironment(
      for {
        actorSystem <- ZManaged.service[ZActorSystem]
        properties  <- ZManaged.service[ZProductsConfigurationProperties]
        httpServer  <- make(actorSystem, properties).map(ZEnvironment[Future[Http.ServerBinding]](_))
      } yield httpServer
    )

  def make(
    actorSystem: ActorSystem[_],
    properties: ProductsConfigurationProperties
  ): ZManaged[ZEnv, Throwable, Future[Http.ServerBinding]] =
    ZManaged.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      ZManaged.acquireReleaseAttemptWith {
        implicit lazy val untypedSystem: actor.ActorSystem = actorSystem.toClassic
        Http()
          .newServerAt(properties.interface, properties.port)
          .bind(
            SwaggerApi.route ~ new ProductsApi(new SlickProductsRepository() {
              override def db: UIO[BasicBackend#DatabaseDef] = ZIO.succeed(Database.forConfig("infrastructure.h2"))
            }).route
          )
      }(_.map(_.terminate(10.second))(runtime.runtimeConfig.executor.asExecutionContext))
        .tapError(exception =>
          ZManaged.succeed(actorSystem.log.error(s"Server could not start with parameters [host:port]=[${properties.interface},${properties.port}]", exception))
        )
        .tap(future =>
          ZManaged.succeed(
            future
              .map(server => actorSystem.log.info(s"Server online at http://${server.localAddress}/docs"))(runtime.runtimeConfig.executor.asExecutionContext)
          )
        )
    }
}

object AkkaHttpServerConfiguration extends AkkaHttpServerConfiguration
