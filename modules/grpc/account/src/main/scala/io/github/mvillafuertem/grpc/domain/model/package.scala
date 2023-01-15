package io.github.mvillafuertem.grpc.domain

import cats.data.ValidatedNel
import io.github.mvillafuertem.grpc.domain.error.AccountError

package object model {

  type Validation[T] = ValidatedNel[AccountError, T]

}
