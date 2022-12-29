package io.github.mvillafuertem.grpc

import cats.effect.{ IO, IOApp }
import io.github.mvillafuertem.grpc.configuration.GreeterConfiguration

// sbt -mem 6000 "grpc-greeter/run"
// docker-compose -f modules/grpc/greeter/src/main/resources/docker-compose.yml up
object GreeterApp extends IOApp.Simple with GreeterConfiguration {

  override def run: IO[Unit] = resource
    .evalMap(server => IO.delay(server.build().start()))
    .use(_ => IO.never[Unit])

}
