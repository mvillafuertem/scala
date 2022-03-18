package io.github.mvillafuertem.tapir.configuration

import io.github.mvillafuertem.tapir.configuration.ActorSystemConfiguration.ZActorSystem
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.ZIO.logError
import zio.{ ExitCode, URIO, ZEnv, ZLayer }

trait ProductsServiceConfiguration extends AuditingConfiguration with zio.ZIOAppDefault {

  val productsServiceApplication: URIO[ZEnv, ExitCode] =
    AkkaHttpServerConfiguration.live.build.useForever
      .provideSomeLayer[ZEnv](
        ZLayer.make[ZActorSystem with ZProductsConfigurationProperties](
          ProductsConfigurationProperties.live,
          ActorSystemConfiguration.live,
          ZLayer.Debug.mermaid
        )
      )
      .catchAll(e => logError(e.getMessage))
      .exitCode

}
