package io.github.mvillafuertem.tapir

import io.github.mvillafuertem.tapir.configuration.ProductsServiceConfiguration
import zio.{ ExitCode, ZIO }

// open http://localhost:8080/api/v1.0/docs/docs.yaml
object ProductsServiceApplication extends ProductsServiceConfiguration with zio.ZIOAppDefault {

  def run: ZIO[zio.ZEnv, Nothing, ExitCode] = productsServiceApplication

}
