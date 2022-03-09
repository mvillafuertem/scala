package io.github.mvillafuertem.tapir.api

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import sttp.tapir.openapi.circe.yaml.RichOpenAPI

final class SwaggerApiSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "schema" in {
    println(SwaggerApi.openApi.toYaml)
    true shouldBe true
  }

}
