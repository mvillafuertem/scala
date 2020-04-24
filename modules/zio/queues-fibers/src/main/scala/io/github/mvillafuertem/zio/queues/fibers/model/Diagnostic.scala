package io.github.mvillafuertem.zio.queues.fibers.model

sealed trait Diagnostic

object Diagnostic {

  case object HipDiagnostic extends Diagnostic

  case object KneeDiagnostic extends Diagnostic

}
