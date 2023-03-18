package io.github.mvillafuertem.grpc.domain.application

import io.grpc.Metadata

trait UseCaseTypeMembers {

  type Request
  type Response
  type Monad[_]

  def apply(request: Request, metadata: Metadata): Monad[Response]

}

object UseCaseTypeMembers {

  type AuX[_] = UseCaseTypeMembers { type Monad[_] = AuX[_] }

  def apply[F[_]: AuX]: AuX[F] = implicitly[AuX[F]]

  implicit val a = new AuX[Option[_]] { self =>
    override type Request         = String
    override type Response        = String
    override type Monad[Response] = Option[Response]

    def apply(request: Request, metadata: Metadata): Monad[Response] = Option("")

  }

  // def useCase[F[_]: AuX] = UseCaseTypeMembers[F].apply("", new Metadata())

  private val value: Option[String] = a.apply("", new Metadata())

  println(value)

}
