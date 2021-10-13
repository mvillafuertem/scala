import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.lihaoyi::mainargs:0.2.1`
import $ivy.`com.softwaremill.sttp.tapir::tapir-akka-http-server:0.17.19`
import $ivy.`com.softwaremill.sttp.tapir::tapir-json-circe:0.17.19`
import $ivy.`dev.zio::zio-logging-slf4j:0.5.12`
import $ivy.`dev.zio::zio:1.0.11`
import MinimalScriptZioApp.Config
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import io.circe.Json
import mainargs.{ arg, main, ParserForClass }
import org.slf4j.{ Logger, LoggerFactory }
import sttp.tapir.json.circe._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.{ anyJsonBody, endpoint, paths, stringBody }
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{ log, Logging }
import zio.{ ExitCode, Has, Task, URIO, ZEnv, ZLayer, ZManaged }

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

@main(
  name = "minimal-script-zio-app",
  doc = ""
)
def minimalScriptZioApp(config: Config): Unit =
  MinimalScriptZioApp.main(Array(config.interface, config.port.toString))

object MinimalScriptZioApp extends zio.App {

  val loggerLayer = Slf4jLogger.make((_, message) => message)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = (for {
    server <- AkkaHttpServer.make(args.head, args(1).toInt).useForever.forkDaemon
    _      <- log.info(s"Press Any Key to stop the demo server") *> zio.console.getStrLn
    _      <- server.interrupt
  } yield ())
    .catchAll(e => log.error(s"${e.getClass.getName} : ${e.getMessage}"))
    .provideSomeLayer[ZEnv](
      loggerLayer >+>
        Routes.live >+>
        AkkaHttpServer.actorSystemLive
    )
    .exitCode

  // Configuration
  implicit def configParser: ParserForClass[Config] = ParserForClass[Config]
  case class Config(
                     @arg(short = 'i', doc = "")
                     interface: String = "0.0.0.0",
                     @arg(name = "p", doc = "")
                     port: Int = 8080
                   )
  object Routes {
    val live: ZLayer[Any, Throwable, Has[Route]] = (for {
      runtime        <- ZManaged.runtime[Any]
      serverLogic     = (method: String) =>
        (paths: List[String], input: Json) =>
          runtime.unsafeRunToFuture(
            Task
              .effect(Right[Unit, String]("ok"))
              .tap(_ => log.info(s"""$method
                                    |/${paths.mkString("/")}
                                    |${input.spaces2}
                                    |""".stripMargin))
              .provideLayer(loggerLayer)
          )
      serverEndpoints =
        List(
          endpoint.get
            .in("hello")
            .out(stringBody)
            .serverLogic[Future](_ => runtime.unsafeRunToFuture(Task.effect(Right[Unit, String]("Hello World!")))),
          endpoint.put
            .in(paths)
            .in(anyJsonBody[Json])
            .out(stringBody)
            .serverLogic[Future](serverLogic("POST").tupled),
          endpoint.post
            .in(paths)
            .in(anyJsonBody[Json])
            .out(stringBody)
            .serverLogic[Future](serverLogic("POST").tupled)
        )
      route          <- ZManaged.effect(AkkaHttpServerInterpreter.toRoute(serverEndpoints))
    } yield route).toLayer

  }
  object AkkaHttpServer {

    def actorSystemLive: ZLayer[Any, Throwable, Has[ActorSystem]] = (for {
      runtime     <- ZManaged.runtime[Any]
      actorSystem <- ZManaged
        .make(Task(ActorSystem("minimal-akka-http-with-zio", defaultExecutionContext = Some(runtime.platform.executor.asEC))))(e =>
          Task.fromFuture(_ => e.terminate()).orDie
        )
    } yield actorSystem).toLayer

    def make(interface: String, port: Int): ZManaged[Has[Route] with Has[ActorSystem] with Logging, Throwable, Http.ServerBinding] =
      for {
        route       <- ZManaged.access[Has[Route]](_.get)
        actorSystem <- ZManaged.access[Has[ActorSystem]](_.get)
        httpServer  <- ZManaged
          .make(
            Task.fromFuture { implicit ec: ExecutionContext =>
              Http()(actorSystem).newServerAt(interface, port).bind(Route.toFunction(route)(actorSystem))
            }.tapError(exception => log.throwable(s"Server could not start with parameters [host:port]=[${interface},${port}]", exception))
              .tap(serverBinding => log.info(s"Server online at http:/${serverBinding.localAddress}"))
          )(serverBinding => Task.fromFuture { implicit ec: ExecutionContext => serverBinding.terminate(10.second) }.orDie)
      } yield httpServer
  }
}
