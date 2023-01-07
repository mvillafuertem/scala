package io.github.mvillafuertem.grpc.application

import cats.Applicative
import cats.data.Validated
import io.github.mvillafuertem.grpc.domain.model.Validation
import io.github.mvillafuertem.grpc.greeter.{ HelloReply, HelloRequest }
import io.grpc.Metadata
import cats.syntax.all._
import io.github.mvillafuertem.grpc.domain.application.UseCase
import io.github.mvillafuertem.grpc.domain.error.AccountError

object VerifyGreeterUseCase {

  def apply[F[_]: Applicative]: UseCase[F, HelloRequest, HelloReply] = new UseCase[F, HelloRequest, HelloReply] {
    override def apply(request: HelloRequest, metadata: Metadata): F[Validation[HelloReply]] =
      Validated
        .cond(request.name.length > 2, HelloReply(request.name), AccountError.BankIsInvalid)
        .toValidatedNel[AccountError, HelloReply]
        .pure[F]
  }

}
