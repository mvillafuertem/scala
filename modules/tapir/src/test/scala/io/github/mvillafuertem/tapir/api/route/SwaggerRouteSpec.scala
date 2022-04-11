package io.github.mvillafuertem.tapir.api.route

import io.github.mvillafuertem.tapir.api.routes.SwaggerRoute
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import sttp.tapir.openapi.circe.yaml.RichOpenAPI

final class SwaggerRouteSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "schema" in {
    println(SwaggerRoute.openApi.toYaml)
    true shouldBe true
  }

}
