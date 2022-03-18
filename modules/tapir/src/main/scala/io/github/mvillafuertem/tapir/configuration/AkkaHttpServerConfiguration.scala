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
import zio.{ Runtime, Task, UIO, ZEnv, ZEnvironment, ZIO, ZLayer, ZManaged }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait AkkaHttpServerConfiguration {

  type ZAkkaHttpServer = Http.ServerBinding

  val live: ZLayer[ZEnv with ZActorSystem with ZProductsConfigurationProperties, Throwable, ZAkkaHttpServer] =
    ZLayer.fromManagedEnvironment(
      for {
        actorSystem <- ZManaged.service[ZActorSystem]
        properties  <- ZManaged.service[ZProductsConfigurationProperties]
        httpServer  <- make(actorSystem, properties).map(ZEnvironment[ZAkkaHttpServer](_))
      } yield httpServer
    )

  def make(
    actorSystem: ActorSystem[_],
    properties: ProductsConfigurationProperties
  ): ZManaged[ZEnv, Throwable, ZAkkaHttpServer] =
    ZManaged.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      ZManaged.acquireReleaseWith {
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
      }(httpServer => Task.fromFuture { implicit ec: ExecutionContext => httpServer.terminate(30.second) }.orDie)
        .tapError(exception =>
          ZManaged.succeed(actorSystem.log.error(s"Server could not start with parameters [host:port]=[${properties.interface},${properties.port}]", exception))
        )
        .tap(server => ZManaged.succeed(actorSystem.log.info(s"Server online at http://${server.localAddress}/docs")))
    }
}

object AkkaHttpServerConfiguration extends AkkaHttpServerConfiguration
