package io.github.mvillafuertem.products.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.github.mvillafuertem.products.BuildInfo
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._


trait SwaggerApi {

  private lazy val openApi: String = Seq(
    // p r o d u c t s  e n d p o i n t
    ProductsEndpoint.productsEndpoint
  ).toOpenAPI(BuildInfo.name, BuildInfo.version)
    .toYaml

  private lazy val contextPath = "docs"
  private lazy val yamlName = "docs.yaml"

  lazy val route: Route = pathPrefix(ProductsEndpoint.apiResource / ProductsEndpoint.apiVersion) {pathPrefix(contextPath) {
    pathEndOrSingleSlash {
      redirect(s"$contextPath/index.html?url=/${ProductsEndpoint.apiResource}/${ProductsEndpoint.apiVersion}/$contextPath/$yamlName", StatusCodes.PermanentRedirect)
    } ~ path(yamlName) {
      complete(openApi)
    } ~ getFromResourceDirectory("META-INF/resources/webjars/swagger-ui/3.24.0/")
  }
  }

}

object SwaggerApi extends SwaggerApi
