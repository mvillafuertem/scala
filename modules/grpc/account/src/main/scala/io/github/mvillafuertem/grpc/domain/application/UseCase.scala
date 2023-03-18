package io.github.mvillafuertem.grpc.domain.application

import cats.Applicative
import cats.data.Validated
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import io.github.mvillafuertem.grpc.domain.error.AccountError
import io.github.mvillafuertem.grpc.domain.model.Validation
import io.github.mvillafuertem.grpc.greeter.{HelloReply, HelloRequest}
import io.grpc.Metadata

// type UseCase[F[_], A <: HelloRequest, R <: HelloReply] = ((A, Metadata) => F[Validation[R]])
trait UseCase[F[_], A <: HelloRequest, R <: HelloReply] extends ((A, Metadata) => F[Validation[R]])

object TestType extends IOApp {

  def apply[F[_]: Applicative]: UseCase[F, HelloRequest, HelloReply] = new UseCase[F, HelloRequest, HelloReply] {
    override def apply(request: HelloRequest, metadata: Metadata): F[Validation[HelloReply]] =
      Validated
        .cond(request.name.length > 2, HelloReply(request.name), AccountError.BankIsInvalid)
        .toValidatedNel[AccountError, HelloReply]
        .pure[F]
  }
  trait MyUseCase {
    type Request
    type Response
    type Monad[_]
    def apply(request: Request, metadata: Metadata): Monad[Response]
  }


  def validate[X[_]: Applicative] = new MyUseCase {
    type Request = String

    type Response = String

    type Monad[_] = Option[_]

    override def apply(request: String, metadata: Metadata): Monad[Response] = Applicative.apply[Option].pure("")

  }


  type MT[A] = MyUseCase { type Monad = MT[A] }
  def validate[F[_]]: String = ???



  override def run(args: List[String]): IO[ExitCode] = IO().as(ExitCode.Success)
}
