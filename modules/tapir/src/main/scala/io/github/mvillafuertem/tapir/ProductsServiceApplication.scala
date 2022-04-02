package io.github.mvillafuertem.tapir

import io.github.mvillafuertem.tapir.configuration.ProductsServiceConfiguration
import zio.{ ExitCode, RuntimeConfigAspect, Scope, ZIO }

// open http://localhost:8080/api/v1.0/docs/docs.yaml
object ProductsServiceApplication extends ProductsServiceConfiguration {

  override def hook: RuntimeConfigAspect = logAspect

  def run: ZIO[zio.ZEnv with Scope, Nothing, ExitCode] = productsServiceApplication

}
