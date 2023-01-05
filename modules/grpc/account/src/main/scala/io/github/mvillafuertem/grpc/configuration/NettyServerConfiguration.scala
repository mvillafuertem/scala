package io.github.mvillafuertem.grpc.configuration

import io.grpc.ServerServiceDefinition
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.{ HealthStatusManager, ProtoReflectionService }
import io.grpc

object NettyServerConfiguration {

  private def nettyServer(service: ServerServiceDefinition, port: Int): grpc.Server = NettyServerBuilder
    // .forAddress(new InetSocketAddress(port))
    .forPort(port)
    .addService(service)
    .addService(new HealthStatusManager().getHealthService)
    .addService(ProtoReflectionService.newInstance())
    .build()

}
