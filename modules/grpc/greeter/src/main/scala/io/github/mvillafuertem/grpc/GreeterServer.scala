package io.github.mvillafuertem.grpc

import cats.effect.{ IO, IOApp, Resource }
import io.github.mvillafuertem.grpc.greeter.{ GreeterFs2Grpc, HelloReply, HelloRequest }
import io.grpc.Metadata
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.{ HealthStatusManager, ProtoReflectionService }

import java.net.InetSocketAddress

// sbt -mem 6000 "grpc-greeter/run"
// docker-compose -f modules/grpc/greeter/src/main/resources/docker-compose.yml up
object GreeterServer extends IOApp.Simple {

  private def resource: Resource[IO, NettyServerBuilder] =
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

  override def run: IO[Unit] = resource
    .evalMap(server => IO.delay(server.build().start()))
    .use(_ => IO.never[Unit])
}
