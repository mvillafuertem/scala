package io.github.mvillafuertem.tapir.api.routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import io.github.mvillafuertem.tapir.BuildInfo
import io.github.mvillafuertem.tapir.api.ProductsEndpoint
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.openapi.{ Info, OpenAPI }
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.{ SwaggerUI, SwaggerUIOptions }

import scala.concurrent.Future

trait SwaggerRoute {

  lazy val openApi: OpenAPI =
    OpenAPIDocsInterpreter()
      .toOpenAPI(
        // p r o d u c t s  e n d p o i n t
        ProductsEndpoint.productsEndpoint,
        Info(BuildInfo.name, BuildInfo.version)
      )

  private lazy val contextPath = "docs"
  private lazy val yamlName    = "docs.yaml"

  lazy val route: Route = DebuggingDirectives.logRequestResult("swagger-logger") {
    AkkaHttpServerInterpreter()
      .toRoute(
        SwaggerUI[Future](openApi.toYaml, SwaggerUIOptions(List(ProductsEndpoint.apiResource, ProductsEndpoint.apiVersion, contextPath), yamlName, Nil))
      )
  }

}

object SwaggerRoute extends SwaggerRoute
