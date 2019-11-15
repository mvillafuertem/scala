package io.github.mvillafuertem.products.configuration

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.{Done, actor}
import io.github.mvillafuertem.products.BuildInfo
import io.github.mvillafuertem.products.api.{ProductsApi, SwaggerApi}
import io.github.mvillafuertem.products.infrastructure.SlickProductsRepository
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.{Task, UIO, ZIO}
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

trait ProductsServiceConfiguration extends InfrastructureConfiguration {

  implicit val executionContext: ExecutionContext

  def httpServer(actorSystem: ActorSystem[_]): Task[Http.ServerBinding] =
    for {
      //actorSystem <- ZIO.environment[ActorSystem[_]]
      server <- Task.fromFuture(_ => {
        implicit lazy val untypedSystem: actor.ActorSystem = actorSystem.toClassic
        implicit lazy val materializer: Materializer = Materializer(actorSystem)
        Http()(untypedSystem).bindAndHandle(SwaggerApi.route ~ new ProductsApi(new SlickProductsRepository() {
          override def db: UIO[BasicBackend#DatabaseDef] =  ZIO.effectTotal(Database.forConfig("infrastructure.h2"))
        }).route, productsConfigurationProperties.interface, productsConfigurationProperties.port)
      }).mapError { exception =>
        actorSystem.log.error(s"Server could not start with parameters [host:port]=[${productsConfigurationProperties.interface},${productsConfigurationProperties.port}]", exception)
        exception
      }
      _ <- UIO.effectTotal(actorSystem.log.info(s"Server online at http://${server.localAddress.getHostString}:${server.localAddress.getPort}/"))
    } yield server


  val actorSystem: Task[ActorSystem[Done]] = Task(ActorSystem[Done](Behaviors.setup[Done] { context =>
    context.setLoggerName(this.getClass)
    context.log.info(s"Starting ${productsConfigurationProperties.name}... ${BuildInfo.toJson}")
    Behaviors.receiveMessage {
      case Done =>
        context.log.error(s"Server could not start!")
        Behaviors.stopped
    }
  }, "ProductsServiceApplication"))


  lazy val productsConfigurationProperties = ProductsConfigurationProperties()

  override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forConfig("infrastructure.h2"))

}
