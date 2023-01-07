package io.github.mvillafuertem.grpc.application

import cats.effect.{ Async, MonadCancelThrow, Resource }
import cats.syntax.flatMap._
import cats.syntax.validated._
import io.github.mvillafuertem.grpc.domain.application.UseCase
import io.github.mvillafuertem.grpc.domain.error.AccountError
import io.github.mvillafuertem.grpc.domain.model.Validation
import io.github.mvillafuertem.grpc.greeter.{ GreeterFs2Grpc, HelloReply, HelloRequest }
import io.grpc.{ Metadata, ServerServiceDefinition }

object GreeterGrpc {

  object Service {

    private def create[F[_]: MonadCancelThrow](verifyGreeterUseCase: UseCase[F, HelloRequest, HelloReply]): GreeterFs2Grpc[F, Metadata] = ???
//      new GreeterFs2Grpc[F, Metadata] {
//        override def sayHello(request: HelloRequest, ctx: Metadata): F[HelloReply] = {
//          request
//            .valid[AccountError]
//            .traverse(verifyGreeterUseCase(_, ctx))
//            .flatMap(_.leftMap(new RuntimeException(_)).liftTo[F])
//          ???
//        }
//      }

    def getServiceDefinition[F[_]: Async](
      verifyGreeterUseCase: UseCase[F, HelloRequest, HelloReply]
    ): Resource[F, ServerServiceDefinition] =
      GreeterFs2Grpc
        .bindServiceResource(Service.create(verifyGreeterUseCase))

  }

}
