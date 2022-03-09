package io.github.mvillafuertem.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation._
import io.circe.generic.auto._
import pdi.jwt.{ JwtAlgorithm, JwtCirce, JwtClaim }
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.akkahttp._

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

object AkkaHTTPWithJWT extends App {

  // A K K A
  private implicit val actorSystem: ActorSystem             = ActorSystem("akka-http-with-jwt")
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher

  // D O M A I N
  final case class LoginRequest(username: String, password: String)

  // L O G I C
  lazy val key       = "secretKey"
  lazy val algorithm = JwtAlgorithm.HS256

  private def setClaims(username: String): JwtClaim = {
    val now = Instant.now
    JwtClaim(
      content = s"""{"username": "$username"}""",
      expiration = Some(now.plus(2, ChronoUnit.HOURS).getEpochSecond),
      issuedAt = Some(now.getEpochSecond)
    )
  }

  private def isTokenExpired(jwt: String): Boolean = JwtCirce.decode(jwt, key, Seq(algorithm)) match {
    case Failure(exception) =>
//      actorSystem.log.error(exception.getMessage, exception)
      true
    case Success(value)     => value.expiration.exists(_ < Instant.now.getEpochSecond)
  }

  private def isTokenValid(jwt: String): Boolean = JwtCirce
    .isValid(jwt, key, Seq(algorithm))

  // R O U T E
  type AuthToken = String
  final lazy val AccessTokenHeaderName = "X-Auth-Token"

  lazy val auth = AkkaHttpServerInterpreter()
    .toRoute(
      endpoint.post
        .in("auth" / "login")
        .in(jsonBody[LoginRequest])
        .out(statusCode(StatusCode.Ok).and(header[AuthToken](AccessTokenHeaderName)))
        .errorOut(statusCode(StatusCode.Unauthorized))
        .serverLogic[Future] {
          case admin @ LoginRequest("admin", "admin") =>
            Future.successful {
              val claim = setClaims(admin.username)
              val token = JwtCirce.encode(claim, key, algorithm)
              Right[Unit, AuthToken](token)
            }
          case LoginRequest(_, _)                     => Future.successful(Left[Unit, String]("Unauthorized"))
        }
    )

  lazy val securedContent = AkkaHttpServerInterpreter()
    .toRoute(
      endpoint.get
        .in("hello")
        .in(header[AuthToken](AccessTokenHeaderName))
        .out(stringBody)
        .errorOut(statusCode(StatusCode.Unauthorized).and(stringBody))
        .serverLogic[Future] {
          case authToken if isTokenExpired(authToken) => Future.successful(Left[String, String]("Session expired"))
          case authToken if isTokenValid(authToken)   => Future.successful(Right[String, String]("Hello World!"))
          case _                                      => Future(Left[String, String]("Session expired"))
        }
    )

  // R U N  A P P L I C A T I O N
  val serverBinding: Future[Http.ServerBinding] =
    Http().newServerAt("localhost", 8080).bind(auth ~ securedContent)

  serverBinding.onComplete {
    case Success(bound) =>
      actorSystem.log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")

    case Failure(e) =>
      actorSystem.log.error("Server error", e)
      actorSystem.terminate()
  }

  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
