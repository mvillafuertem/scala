package io.github.mvillafuertem.todo.api

import java.nio.charset.StandardCharsets

import akka.NotUsed
import akka.actor.typed.Logger
import akka.http.scaladsl.server.Directives.{complete, get, path, pathEnd}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.scaladsl.Source
import io.circe.{Decoder, Encoder, HCursor, Json}
import tapir.model.{StatusCode, StatusCodes}
import tapir.{Endpoint, endpoint, jsonBody, oneOf, statusCode, statusDefaultMapping, statusMapping, _}
import io.circe.generic.auto._
import io.github.mvillafuertem.todo.BuildInfo
import io.github.mvillafuertem.todo.api.ToDoAPI.{BuildInfo, _}
import tapir.Codec.JsonCodec
import tapir.DecodeResult.{Error, Value}
import io.circe.generic.auto._
import io.circe.parser.{decode, _}
import io.circe.syntax._
import tapir._
import tapir.json.circe._
import tapir.openapi.OpenAPI
import tapir.docs.openapi._
import tapir.openapi.OpenAPI
import tapir.openapi.circe.yaml._
import tapir.swagger.akkahttp.SwaggerAkka
import tapir.server.akkahttp._

import scala.concurrent.Future

/**
 * @author Miguel Villafuerte
 */
final class ToDoAPI(log: Logger) {

  import akka.http.scaladsl.server.Directives._

  val routes: Route = new SwaggerAkka(yml).routes ~ route


  lazy val route: Route = DebuggingDirectives.logRequestResult("actuator-logger") {
    actuatorEndpoint.toRoute { _ =>
      val buildInfo = BuildInfo.toMap
      log.info(s"build-info: $buildInfo")
      Future.successful(Right(buildInfo))
    }
  }

}

object ToDoAPI {

  def apply(log: Logger): ToDoAPI = new ToDoAPI(log)


  lazy val openApi: OpenAPI = List(actuatorEndpoint).toOpenAPI("ToDo API", "1.0")
  lazy val yml: String = openApi.toYaml

  final case class ErrorInfo(
                              code: String,
                              message: String
                            )

  type HttpError = (StatusCode, ErrorInfo)
  type AuthToken = String
  type DoorId = String
  type BuildInfo = Map[String, Any]


  implicit val encodeBuildInfo: Encoder[BuildInfo] = (a: BuildInfo) =>
    Json.fromFields(a.map{case (a, b) => (a, Json.fromString(String.valueOf(b)))})

  implicit val decodeBuildInfo: Decoder[BuildInfo] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    version <- c.downField("version").as[String]
    scalaVersion <- c.downField("scalaVersion").as[String]
    sbtVersion <- c.downField("sbtVersion").as[String]
    gitCommit <- c.downField("gitCommit").as[String]
    builtAtString <- c.downField("builtAtString").as[String]
    builtAtMillis <- c.downField("builtAtMillis").as[String]
  } yield Map[String, Any](
    "name" -> name,
    "version" -> version,
    "scalaVersion" -> scalaVersion,
    "sbtVersion" -> sbtVersion,
    "gitCommit" -> gitCommit,
    "builtAtString" -> builtAtString,
    "builtAtMillis" -> builtAtMillis)


//  implicit val buildInfoCodec: JsonCodec[BuildInfo] =
//    implicitly[JsonCodec[String]].map(BuildInfo.toMap)(io.circe.parser.decode[BuildInfo].asJson)
//


  lazy val baseEndpoint: Endpoint[Unit, HttpError, Unit, Nothing] =
    endpoint
      .in("api" / "v1.0")
      .errorOut(
        oneOf(
          statusMapping(
            StatusCodes.BadRequest,
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(badRequestErrorInfo)
                .description("Bad Request"))
          ),
          statusMapping(
            StatusCodes.Unauthorized,
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(unauthorizedErrorInfo)
                .description("Unauthorized"))
          ),
          statusMapping(
            StatusCodes.Forbidden,
            statusCode.and(jsonBody[ErrorInfo]
              .example(forbiddenErrorInfo)
              .description("Forbidden"))
          ),
          statusMapping(
            StatusCodes.NotFound,
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(notFoundErrorInfo)
                .description("Not Found"))
          ),
          statusMapping(
            StatusCodes.InternalServerError,
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(internalServerErrorErrorInfo)
                .description("Internal Server Error"))),
          statusMapping(
            StatusCodes.ServiceUnavailable,
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(serviceUnavailableErrorInfo)
                .description("Service Unavailable"))),
          statusDefaultMapping(
            statusCode
              .and(jsonBody[ErrorInfo]
                .example(unknownErrorInfo)
                .description("unknown error")))
        )
      )


  lazy val actuatorEndpoint: Endpoint[Unit, StatusCode, BuildInfo, Nothing] =
    endpoint
      .in("api" / "v1.0")
      .name("service-health")
      .description("ToDo Application Service Health Check Endpoint")
      .get
      .in("health")
      .out(jsonBody[BuildInfo].example(BuildInfo.toMap))
      .errorOut(statusCode)


  // 400
  lazy val badRequestErrorInfo = ErrorInfo("bad_request", "The server could not understand the request due to invalid syntax")
  lazy val unauthorizedErrorInfo = ErrorInfo("unauthorized", "The client must authenticate itself to get the requested response")
  lazy val forbiddenErrorInfo = ErrorInfo("forbidden", "The client does not have access rights to the content")
  lazy val notFoundErrorInfo = ErrorInfo("not_found", "The server can not find requested resource")
  lazy val conflictErrorInfo = ErrorInfo("conflict", "This response is sent when a request conflicts with the current state of the server")
  // 500
  lazy val internalServerErrorErrorInfo = ErrorInfo("internal_server_error", "The server has encountered a situation it doesn't know how to handle")
  lazy val serviceUnavailableErrorInfo = ErrorInfo("service_unavailable", "The server is not ready to handle the request")
  lazy val unknownErrorInfo = ErrorInfo("unknown_error", "The reason for the error could not be determined")

}
