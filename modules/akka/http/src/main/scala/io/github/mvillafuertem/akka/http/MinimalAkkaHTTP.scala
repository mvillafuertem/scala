package io.github.mvillafuertem.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import sttp.tapir._
import sttp.tapir.server.akkahttp._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object MinimalAkkaHTTP extends App {

  // A K K A
  private implicit val actorSystem: ActorSystem        = ActorSystem("minimal-akka-http")
  private implicit val actorMaterializer: Materializer = Materializer(actorSystem)
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  // R O U T E
  val route = endpoint.get
    .in("hello")
    .out(stringBody)
    .toRoute(_ => Future(Right("Hello World!")))

  // R U N A P P L I C A T I O N
  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(route, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      actorSystem.log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")

    case Failure(e) =>
      actorSystem.log.error("Server error", e)
      actorSystem.terminate()
  }

  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
