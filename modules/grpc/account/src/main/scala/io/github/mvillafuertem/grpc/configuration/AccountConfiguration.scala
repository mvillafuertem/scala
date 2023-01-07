package io.github.mvillafuertem.grpc.configuration

import cats.effect.{ IO, Resource }
import com.linecorp.armeria
import io.github.mvillafuertem.grpc.account.AccountFs2Grpc
import io.github.mvillafuertem.grpc.application.AccountCreator

trait AccountConfiguration {

  def resource: Resource[IO, armeria.server.Server] =
    AccountFs2Grpc
      .bindServiceResource(AccountCreator())
      .map(ArmeriaServerConfiguration.armeriaServer(_, port = 9999))

}
