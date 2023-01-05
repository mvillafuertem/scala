package io.github.mvillafuertem.grpc.application

import cats.Applicative
import cats.data.Validated
import io.github.mvillafuertem.grpc.domain.UseCase
import io.github.mvillafuertem.grpc.domain.Validation.{ Validation, ValidationError }
import io.github.mvillafuertem.grpc.greeter.{ HelloReply, HelloRequest }
import io.grpc.Metadata
import cats.syntax.all._

object VerifyGreeterUseCase {

  def apply[F[_]: Applicative]: UseCase[F, HelloRequest, HelloReply] = new UseCase[F, HelloRequest, HelloReply] {
    override def apply(request: HelloRequest, metadata: Metadata): F[Validation[HelloReply]] = {
      val value: Validation[HelloReply] = Validated
        .cond(request.name.length > 2, HelloReply(request.name), "invalid name")
        .toValidatedNel[ValidationError, HelloReply]
      value.pure[F]
    }
  }

}
