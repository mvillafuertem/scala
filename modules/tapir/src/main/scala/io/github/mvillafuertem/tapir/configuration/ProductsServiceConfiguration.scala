package io.github.mvillafuertem.tapir.configuration

import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import zio.{ ExitCode, URIO, ZEnv, ZLayer }

trait ProductsServiceConfiguration {

  val productsServiceApplication: URIO[ZEnv, ExitCode] =
    AkkaHttpServerConfiguration.live.build.useForever
      .provideSomeLayer[ZEnv](ZLayer.succeed[ProductsConfigurationProperties](ProductsConfigurationProperties()) >+> ActorSystemConfiguration.live)
      .fold(
        e => {
          println(e)
          ExitCode.failure
        },
        _ => ExitCode.success
      )

}
