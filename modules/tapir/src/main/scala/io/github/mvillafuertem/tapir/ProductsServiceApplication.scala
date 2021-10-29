package io.github.mvillafuertem.tapir

import io.github.mvillafuertem.tapir.configuration.ProductsServiceConfiguration
import zio.{ ExitCode, Managed, UIO, ZIO }

import scala.concurrent.ExecutionContext

object ProductsServiceApplication extends ProductsServiceConfiguration with zio.App {

  override implicit val executionContext: ExecutionContext = platform.executor.asEC

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    Managed
      .make(actorSystem)(sys => UIO.succeed(sys.terminate()).ignore)
      .use(actorSystem =>
        for {
          _ <- httpServer(actorSystem)
        } yield 0
      )
      .exitCode

}
