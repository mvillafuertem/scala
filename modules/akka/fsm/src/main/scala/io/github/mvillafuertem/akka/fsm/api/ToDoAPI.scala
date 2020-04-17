package io.github.mvillafuertem.akka.fsm.api

import akka.http.scaladsl.model
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.github.mvillafuertem.akka.fsm.BuildInfo
import org.slf4j.LoggerFactory
import sttp.model.StatusCode
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.akkahttp._
import sttp.tapir.swagger.akkahttp.SwaggerAkka
import sttp.tapir.{Endpoint, endpoint, oneOf, statusCode, statusDefaultMapping, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Miguel Villafuerte
 */
final class ToDoAPI() {

  import ToDoAPI._
  import akka.http.scaladsl.server.Directives._

  private val log = LoggerFactory.getLogger(getClass)

  val routes: Route = new SwaggerAkka(yml).routes ~ route

  def handleErrors[T](f: Future[T]): Future[Either[HttpError, T]] =
    f.transform {
      case Success(v) => Success(Right(v))
      case Failure(e) =>
        log.error("Exception when running endpoint logic", e)
        Success(Left((StatusCode.Ok, conflictErrorInfo)))
    }

  private val getBuildInfo: Unit => Future[HealthInfo] = Unit => Future.successful({
    val buildInfo: HealthInfo = BuildInfo.toMap
    log.info(s"build-info: $buildInfo")
    buildInfo
  })


  lazy val route: Route = DebuggingDirectives.logRequestResult("actuator-logger") {
    actuatorEndpoint.toRoute(getBuildInfo andThen handleErrors)
  }


}

object ToDoAPI {

//  def apply(log: Logger): ToDoAPI = new ToDoAPI(log)
  def apply(): ToDoAPI = new ToDoAPI()

  lazy val openApi: OpenAPI = List(actuatorEndpoint).toOpenAPI("ToDo API", "1.0")
  lazy val yml: String = openApi.toYaml

  final case class ErrorInfo(
                              code: String,
                              message: String
                            )

  type HttpError = (StatusCode, ErrorInfo)
  type AuthToken = String
  type DoorId = String
  type HealthInfo = Map[String, Any]


  implicit val encodeBuildInfo: Encoder[HealthInfo] = (a: HealthInfo) =>
    Json.fromFields(a.map{case (a, b) => (a, Json.fromString(String.valueOf(b)))})

  implicit val decodeBuildInfo: Decoder[HealthInfo] = (c: HCursor) => for {
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

  implicit val buildInfoCodec: JsonCodec[HealthInfo] =
    implicitly[JsonCodec[Json]]
      .map(a => (decode[HealthInfo](a.noSpaces)).getOrElse(BuildInfo.toMap))(a => a.asJson)


  private val notFoundErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(notFoundErrorInfo)
      .description("Not Found"))

  private val internalServerErrorErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(internalServerErrorErrorInfo)
      .description("Internal Server Error"))

  private val serviceUnavailableErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(serviceUnavailableErrorInfo)
      .description("Service Unavailable"))

  private val badRequestErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(badRequestErrorInfo)
      .description("Bad Request"))

  private val unauthorizedErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(unauthorizedErrorInfo)
      .description("Unauthorized"))

  private val forbiddenErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
    .example(forbiddenErrorInfo)
    .description("Forbidden"))

  private val unknownErrorInfoValue: EndpointOutput[(StatusCode, ErrorInfo)] = statusCode
    .and(jsonBody[ErrorInfo]
      .example(unknownErrorInfo)
      .description("unknown error"))

  lazy val baseEndpoint: Endpoint[Unit, HttpError, Unit, Nothing] =
    endpoint
      .in("api" / "v1.0")
      .errorOut(
        oneOf(
          statusMappingValueMatcher(StatusCode.BadRequest, badRequestErrorInfoValue){
            case (StatusCode.BadRequest, _) => true
          },
          statusMappingValueMatcher(StatusCode.Unauthorized, unauthorizedErrorInfoValue){
            case (StatusCode.Unauthorized, _) => true
          },
          statusMappingValueMatcher(StatusCode.Forbidden, forbiddenErrorInfoValue){
            case (StatusCode.Forbidden, _) => true
          },
          statusMappingValueMatcher(StatusCode.NotFound, notFoundErrorInfoValue){
            case (StatusCode.NotFound, _) => true
          },
          statusMappingValueMatcher(StatusCode.InternalServerError, internalServerErrorErrorInfoValue){
            case (StatusCode.InternalServerError, _) => true
          },
          statusMappingValueMatcher(StatusCode.ServiceUnavailable, serviceUnavailableErrorInfoValue){
            case (StatusCode.ServiceUnavailable, _) => true
          },
          statusDefaultMapping(unknownErrorInfoValue)
        )
      )


  lazy val actuatorEndpoint: Endpoint[Unit, HttpError, HealthInfo, Nothing] =
    baseEndpoint
      .name("service-health")
      .description("ToDo Application Service Health Check Endpoint")
      .get
      .in("health")
      .out(anyJsonBody[HealthInfo].example(BuildInfo.toMap))
      //.errorOut(statusCode)


  // 400
  lazy val badRequestErrorInfo = ErrorInfo(model.StatusCodes.BadRequest.reason, model.StatusCodes.BadRequest.defaultMessage)
  lazy val unauthorizedErrorInfo = ErrorInfo(model.StatusCodes.Unauthorized.reason, model.StatusCodes.Unauthorized.defaultMessage)
  lazy val forbiddenErrorInfo = ErrorInfo(model.StatusCodes.Forbidden.reason, model.StatusCodes.Forbidden.defaultMessage)
  lazy val notFoundErrorInfo = ErrorInfo(model.StatusCodes.NotFound.reason, model.StatusCodes.NotFound.defaultMessage)
  lazy val conflictErrorInfo = ErrorInfo(model.StatusCodes.Conflict.reason, model.StatusCodes.Conflict.defaultMessage)
  // 500
  lazy val internalServerErrorErrorInfo = ErrorInfo(model.StatusCodes.InternalServerError.reason, model.StatusCodes.InternalServerError.defaultMessage)
  lazy val serviceUnavailableErrorInfo = ErrorInfo(model.StatusCodes.ServiceUnavailable.reason, model.StatusCodes.ServiceUnavailable.defaultMessage)
  lazy val unknownErrorInfo = ErrorInfo("unknown_error", "The reason for the error could not be determined")

}
