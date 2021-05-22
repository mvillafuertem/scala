package io.github.mvillafuertem.http4s

import cats.data.Kleisli
import org.http4s.{ HttpRoutes, Request, Response }
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.blaze.server.BlazeServerBuilder
import zio._
import zio.console.Console
import zio.interop.catz._

object MinimalHttp4s extends zio.App {

  val dsl: Http4sDsl[Task] = Http4sDsl[Task]
  import dsl._

  val helloWorldService: Kleisli[Task, Request[Task], Response[Task]] = HttpRoutes
    .of[Task] { case GET -> Root / "hello" =>
      Ok("Hello, Joe")
    }
    .orNotFound

  type Http4Server = Has[Server]

  def createHttp4Server: ZManaged[ZEnv, Throwable, Server] =
    ZManaged.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      BlazeServerBuilder[Task](runtime.platform.executor.asEC)
        .bindHttp(8080, "localhost")
        .withHttpApp(helloWorldService)
        .resource
        .toManagedZIO
    }

  def createHttp4sLayer: ZLayer[ZEnv, Throwable, Http4Server] =
    ZLayer.fromManaged(createHttp4Server)

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {

    val program: ZIO[Has[Server] with Console, Nothing, Nothing] =
      ZIO.never

    val httpServerLayer: ZLayer[ZEnv, Throwable, Http4Server] = createHttp4sLayer

    program
      .provideLayer(httpServerLayer ++ Console.live)
      .exitCode
  }

}
