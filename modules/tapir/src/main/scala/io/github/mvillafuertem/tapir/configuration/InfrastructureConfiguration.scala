package io.github.mvillafuertem.tapir.configuration

import slick.basic.BasicBackend
import zio.UIO

trait InfrastructureConfiguration {

  def db: UIO[BasicBackend#DatabaseDef]

}

object InfrastructureConfiguration {}
