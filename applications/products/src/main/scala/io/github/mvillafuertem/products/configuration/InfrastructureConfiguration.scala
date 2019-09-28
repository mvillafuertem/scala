package io.github.mvillafuertem.products.configuration

import slick.basic.BasicBackend
import zio.UIO

trait InfrastructureConfiguration {

  def db: InfrastructureConfiguration.Service

}

object InfrastructureConfiguration {

  trait Service {
    def db: UIO[BasicBackend#DatabaseDef]
  }

}
