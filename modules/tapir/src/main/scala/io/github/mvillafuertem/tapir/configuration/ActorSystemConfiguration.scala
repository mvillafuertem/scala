package io.github.mvillafuertem.tapir.configuration

import akka.Done
import akka.actor.BootstrapSetup
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.{ Runtime, ZLayer, ZManaged }

import scala.concurrent.ExecutionContext

trait ActorSystemConfiguration {

  type ZActorSystem = ActorSystem[Done]

  private def live(executionContext: ExecutionContext, products: ProductsConfigurationProperties): ActorSystem[Done] =
    ActorSystem[Done](
      Behaviors.setup[Done] { context =>
        context.setLoggerName(getClass)
        context.log.info(s"Starting service with configuration ${products.asJson.noSpaces}... ${"BuildInfo.toJson"}")
        Behaviors.receiveMessage { case Done =>
          context.log.error(s"Server could not start!")
          Behaviors.stopped
        }
      },
      products.name.toLowerCase(),
      BootstrapSetup().withDefaultExecutionContext(executionContext)
    )

  val live: ZLayer[ZProductsConfigurationProperties, Throwable, ZActorSystem] =
    ZManaged
      .runtime[ZProductsConfigurationProperties]
      .flatMap { implicit runtime: Runtime[ZProductsConfigurationProperties] => make(runtime.environment.get, runtime.runtimeConfig.executor.asExecutionContext) }
      .toLayer

  def make(products: ProductsConfigurationProperties, executionContext: ExecutionContext): ZManaged[Any, Throwable, ActorSystem[Done]] =
    ZManaged.acquireReleaseAttemptWith(live(executionContext, products))(_.terminate())

}

object ActorSystemConfiguration extends ActorSystemConfiguration
