package io.github.mvillafuertem.tapir.configuration

import akka.Done
import akka.actor.BootstrapSetup
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.{ Runtime, Scope, Task, ZIO, ZLayer }

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

  val live: ZLayer[Scope with ZProductsConfigurationProperties, Throwable, ZActorSystem] =
    ZIO
      .runtime[ZProductsConfigurationProperties]
      .flatMap { implicit runtime: Runtime[ZProductsConfigurationProperties] =>
        make(runtime.environment.get, runtime.runtimeConfig.executor.asExecutionContext)
      }
      .toLayer

  def make(products: ProductsConfigurationProperties, executionContext: ExecutionContext): ZIO[Any with Scope, Throwable, ActorSystem[Done]] =
    ZIO.acquireRelease(Task(live(executionContext, products)))(actorSystem => Task(actorSystem.terminate()).orDie)

}

object ActorSystemConfiguration extends ActorSystemConfiguration
