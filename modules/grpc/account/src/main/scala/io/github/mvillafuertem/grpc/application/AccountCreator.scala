package io.github.mvillafuertem.grpc.application

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.github.mvillafuertem.grpc.account.{ AccountFs2Grpc, Request }
import io.github.mvillafuertem.grpc.account
import io.grpc.Metadata

object AccountCreator {

  def apply(): AccountFs2Grpc[IO, Metadata] = new AccountFs2Grpc[IO, Metadata] {

    override def create(request: Request, ctx: Metadata): IO[account.Response] =
      AccountValidator
        .validate(request)
        .fold(fe => account.Response.of(fe.toString, ""), bankAccount => account.Response.of("OK", bankAccount.toString))
        .pure[IO]

  }

}
