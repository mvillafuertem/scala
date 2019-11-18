package io.github.mvillafuertem.products

import io.github.mvillafuertem.products.configuration.ProductsServiceConfiguration
import zio.{Managed, UIO, ZIO, console}

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
            _ <- console.putStrLn("Type [ENTER] to stop")
            _ <- console.getStrLn
          } yield 0)
      .catchAll(e => console.putStrLn(e.toString).as(1))

}
