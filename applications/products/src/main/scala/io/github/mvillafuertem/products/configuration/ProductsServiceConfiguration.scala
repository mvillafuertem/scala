package io.github.mvillafuertem.products.configuration
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.{Done, actor}
import io.github.mvillafuertem.products.BuildInfo
import io.github.mvillafuertem.products.api.SwaggerApi
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.{Task, UIO, ZIO}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait ProductsServiceConfiguration extends InfrastructureConfiguration {

  implicit val executionContext: ExecutionContext

  def httpServer(actorSystem: ActorSystem[_]): Task[Unit] = Task({

    implicit lazy val untypedSystem: actor.ActorSystem = actorSystem.toClassic
    implicit lazy val materializer: Materializer = Materializer(actorSystem)

    val serverBinding = Http()(untypedSystem)
      .bindAndHandle(routes, productsConfigurationProperties.interface, productsConfigurationProperties.port)

    serverBinding.onComplete {
      case Success(bound) =>
        actorSystem.log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        actorSystem.log.error(s"Server could not start with parameters [host:port]=[${productsConfigurationProperties.interface},${productsConfigurationProperties.port}]", e)
        e.printStackTrace()
    }
  })

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

  val routes = SwaggerApi.route

}
