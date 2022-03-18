package io.github.mvillafuertem.tapir.configuration

import zio.logging.LogFormat
import zio.logging.backend.SLF4J
import zio.{ LogLevel, RuntimeConfigAspect, ZIOAspect }

// @see https://ziverge.com/blog/a-preview-of-logging-in-zio-2/
trait AuditingConfiguration {

  protected val logAspect: RuntimeConfigAspect =
    SLF4J.slf4j(
      logLevel = LogLevel.Debug,
      format = LogFormat.colored
    )

  def correlationAspect(id: String): ZIOAspect[Nothing, Any, Nothing, Any, Nothing, String] =
    ZIOAspect.loggedWith[String](c => s"$c") @@ ZIOAspect.annotated("correlationId", id)

  def genericLog[T](message: String): ZIOAspect[Nothing, Any, Nothing, Any, Nothing, T] =
    ZIOAspect.loggedWith[T](a => s"$message: $a")

}
