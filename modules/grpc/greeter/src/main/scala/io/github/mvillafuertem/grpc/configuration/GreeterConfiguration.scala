package io.github.mvillafuertem.grpc.configuration

import cats.effect.{ IO, Resource }
import io.github.mvillafuertem.grpc.greeter.{ GreeterFs2Grpc, HelloReply, HelloRequest }
import io.grpc.Metadata
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.{ HealthStatusManager, ProtoReflectionService }

import java.net.InetSocketAddress

trait GreeterConfiguration {

  def resource: Resource[IO, NettyServerBuilder] =
    GreeterFs2Grpc
      .bindServiceResource(new GreeterFs2Grpc[IO, Metadata] {
        override def sayHello(request: HelloRequest, ctx: Metadata): IO[HelloReply] = IO(
          HelloReply.of(s"Hola ${request.name}")
        )
      })
      .map(service =>
        NettyServerBuilder
          .forAddress(new InetSocketAddress(9999))
          .addService(service)
          .addService(new HealthStatusManager().getHealthService)
          .addService(ProtoReflectionService.newInstance())
      )

}
