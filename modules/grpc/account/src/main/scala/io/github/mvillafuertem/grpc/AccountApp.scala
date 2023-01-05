package io.github.mvillafuertem.grpc

import cats.effect.{ IO, IOApp }
import io.github.mvillafuertem.grpc.configuration.AccountConfiguration

// sbt -mem 6000 "grpc-account/run"
// docker-compose -f modules/grpc/account/src/main/resources/docker-compose.yml up grpcui
// env UID=$(id -u) GID=$(id -g) docker-compose --file modules/grpc/account/src/main/resources/docker-compose.yml up neo4j
object AccountApp extends IOApp.Simple with AccountConfiguration {

  override def run: IO[Unit] = resource
    .evalMap(server => IO.delay(server.start()))
    .use(_ => IO.never[Unit])

}
