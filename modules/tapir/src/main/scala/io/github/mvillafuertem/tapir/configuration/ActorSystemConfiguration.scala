package io.github.mvillafuertem.tapir.configuration

import akka.Done
import akka.actor.BootstrapSetup
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.github.mvillafuertem.tapir.BuildInfo
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.ZIO.{ logError, logInfo }
import zio.{ Runtime, Task, ZLayer, ZManaged }

import scala.concurrent.ExecutionContext

trait ActorSystemConfiguration {

  type ZActorSystem = ActorSystem[Done]

  private def live(executionContext: ExecutionContext, products: ProductsConfigurationProperties): ActorSystem[Done] =
    ActorSystem[Done](
      Behaviors.setup[Done] { context =>
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
      .flatMap { implicit runtime: Runtime[ZProductsConfigurationProperties] =>
        make(runtime.environment.get, runtime.runtimeConfig.executor.asExecutionContext)
      }
      .toLayer

  def make(products: ProductsConfigurationProperties, executionContext: ExecutionContext): ZManaged[Any, Throwable, ActorSystem[Done]] =
    for {
      _           <- Task(parse(BuildInfo.toJson)).absolve
                       .map(_.deepMerge(products.asJson).noSpacesSortKeys)
                       .foldZIO(
                         exception => logError(s"Error to parse configuration $exception"),
                         configuration => logInfo(s"Starting service with configuration $configuration")
                       )
                       .toManaged
      actorSystem <- ZManaged.acquireReleaseAttemptWith {
                       live(executionContext, products)
                     }(_.terminate())

    } yield actorSystem

}

object ActorSystemConfiguration extends ActorSystemConfiguration
