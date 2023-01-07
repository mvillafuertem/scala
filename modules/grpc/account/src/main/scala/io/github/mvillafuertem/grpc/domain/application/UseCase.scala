package io.github.mvillafuertem.grpc.domain.application

import io.github.mvillafuertem.grpc.domain.model.Validation
import io.github.mvillafuertem.grpc.greeter.{ HelloReply, HelloRequest }
import io.grpc.Metadata

// type UseCase[F[_], A <: HelloRequest, R <: HelloReply] = ((A, Metadata) => F[Validation[R]])
trait UseCase[F[_], A <: HelloRequest, R <: HelloReply] extends ((A, Metadata) => F[Validation[R]])
