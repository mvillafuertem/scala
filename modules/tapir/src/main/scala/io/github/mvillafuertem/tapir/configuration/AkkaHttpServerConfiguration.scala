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
import zio.ZIO.{ log, logError }
import zio.{ Runtime, Scope, Task, UIO, ZEnv, ZEnvironment, ZIO, ZLayer }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait AkkaHttpServerConfiguration {

  type ZAkkaHttpServer = Http.ServerBinding

  val live: ZLayer[ZEnv with Scope with ZActorSystem with ZProductsConfigurationProperties, Throwable, ZAkkaHttpServer] =
    ZLayer.fromZIOEnvironment(
      for {
        actorSystem <- ZIO.service[ZActorSystem]
        properties  <- ZIO.service[ZProductsConfigurationProperties]
        httpServer  <- make(actorSystem, properties).map(ZEnvironment[ZAkkaHttpServer](_))
      } yield httpServer
    )

  def make(
    actorSystem: ActorSystem[_],
    properties: ProductsConfigurationProperties
  ): ZIO[ZEnv with Scope, Throwable, ZAkkaHttpServer] =
    ZIO.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      ZIO
        .acquireRelease(
          Task.fromFuture { implicit ec: ExecutionContext =>
            implicit lazy val untypedSystem: actor.ActorSystem = actorSystem.toClassic
            Http()
              .newServerAt(properties.interface, properties.port)
              .bind(
                SwaggerApi.route ~ new ProductsApi(new SlickProductsRepository() {
                  override def db: UIO[BasicBackend#DatabaseDef] = ZIO.succeed(Database.forConfig("infrastructure.h2"))
                }).route
              )
          }
        )(httpServer => Task.fromFuture { implicit ec: ExecutionContext => httpServer.terminate(30.second) }.orDie)
        .tapError(exception =>
          logError(s"Server could not start with parameters [host:port]=[${properties.interface},${properties.port}] ${exception.getMessage}")
        )
        .tap(server => log(s"Server online at http://${server.localAddress}/docs"))
    }
}

object AkkaHttpServerConfiguration extends AkkaHttpServerConfiguration
