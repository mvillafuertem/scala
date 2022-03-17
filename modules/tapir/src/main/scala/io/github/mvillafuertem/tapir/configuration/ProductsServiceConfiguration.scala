package io.github.mvillafuertem.tapir.configuration

import io.github.mvillafuertem.tapir.configuration.ActorSystemConfiguration.ZActorSystem
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.{ ExitCode, LogLevel, URIO, ZEnv, ZIO, ZLayer }

trait ProductsServiceConfiguration {

  val productsServiceApplication: URIO[ZEnv, ExitCode] =
    AkkaHttpServerConfiguration.live.build.useForever
      .provideSomeLayer[ZEnv](
        ZLayer.make[ZActorSystem with ZProductsConfigurationProperties](
          ProductsConfigurationProperties.live,
          ActorSystemConfiguration.live,
          ZLayer.Debug.mermaid
        )
      )
      .fold(
        e => {
          ZIO.logLevel(LogLevel.Error) {
            ZIO.log(e.getMessage)
          }
          ExitCode.failure
        },
        _ => ExitCode.success
      )
    // @see https://ziverge.com/blog/a-preview-of-logging-in-zio-2/
    // .provideSomeLayer(SLF4J.slf4j(LogLevel.Info, LogFormat.colored).toLayer)

}
