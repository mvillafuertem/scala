package io.github.mvillafuertem.grpc.configuration

import com.linecorp.armeria
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import com.linecorp.armeria.common.scalapb.ScalaPbJsonMarshaller
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import io.github.mvillafuertem.grpc.account
import io.github.mvillafuertem.grpc.account.AccountGrpc
import io.grpc.ServerServiceDefinition
import io.grpc.protobuf.services.ProtoReflectionService

object ArmeriaServerConfiguration {

  def armeriaServer(service: ServerServiceDefinition, port: Int) =
    armeria.server.Server
      .builder()
      .http(port)
      .service(armeriaGrpcService(service))
      // Add DocService for browsing the list of gRPC services and
      // invoking a service operation from a web form.
      // See https://armeria.dev/docs/server-docservice for more information.
      .serviceUnder("/docs", armeriaDocsService)
      .build()

  private def armeriaDocsService =
    DocService
      .builder()
      .exampleRequests(
        AccountGrpc.SERVICE.getName,
        AccountGrpc.METHOD_CREATE.getBareMethodName,
        account.Request.of("ES95", "1095", "1234", "80", "1234567890")
      )
      .build()

  private def armeriaGrpcService(service: ServerServiceDefinition) =
    GrpcService
      .builder()
      // Add your ScalaPB gRPC stub using `bindService()`
      .addService(service)
      .addService(ProtoReflectionService.newInstance())
      .supportedSerializationFormats(GrpcSerializationFormats.values())
      // Register `ScalaPbJsonMarshaller` for supporting gRPC JSON format.
      .jsonMarshallerFactory(_ => ScalaPbJsonMarshaller())
      .enableHttpJsonTranscoding(true)
      .enableUnframedRequests(true)
      .build()

}
