package io.github.mvillafuertem.grpc.domain

import Validation.Validation
import io.github.mvillafuertem.grpc.greeter.{HelloReply, HelloRequest}
import io.grpc.Metadata

trait UseCase[F[_], A <: HelloRequest, R <: HelloReply] extends ((A, Metadata) => F[Validation[R]])
