package io.github.mvillafuertem.tapir

import io.github.mvillafuertem.tapir.configuration.ProductsServiceConfiguration
import zio.{Managed, UIO, ZIO}

import scala.concurrent.ExecutionContext

object ProductsServiceApplication extends ProductsServiceConfiguration
  with zio.App  {

  override implicit val executionContext: ExecutionContext = platform.executor.asEC

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Managed.make(actorSystem)(sys => UIO.succeed(sys.terminate()).ignore)
      .use(
        actorSystem =>
          for {
            _ <- httpServer(actorSystem)
          } yield 0).fold(_ => 1, _ => 0)

}
