package io.github.mvillafuertem.grpc.domain

import cats.data.ValidatedNel

object Validation {

  type ValidationError = String

  type Validation[T] = ValidatedNel[ValidationError, T]

}
