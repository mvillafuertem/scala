package io.github.mvillafuertem.tapir.configuration

import io.github.mvillafuertem.tapir.configuration.ActorSystemConfiguration.ZActorSystem
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties
import io.github.mvillafuertem.tapir.configuration.properties.ProductsConfigurationProperties.ZProductsConfigurationProperties
import zio.{ ExitCode, Scope, URIO, ZEnv, ZIO, ZLayer }

trait ProductsServiceConfiguration extends AuditingConfiguration with zio.ZIOAppDefault {

  val productsServiceApplication: URIO[ZEnv, ExitCode] =
    ZIO
      .scoped(AkkaHttpServerConfiguration.live.build *> ZIO.never)
      .provideSomeLayer[ZEnv](
        ZLayer.make[ZActorSystem with ZProductsConfigurationProperties with Scope](
          ProductsConfigurationProperties.live,
          ActorSystemConfiguration.live,
          ZLayer.Debug.mermaid,
          ZLayer.scope
        )
      )
      .foldCause(
        e => {
          println(e.prettyPrint)
          ExitCode.failure
        },
        _ => ExitCode.success
      )

}
