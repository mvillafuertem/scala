package io.github.mvillafuertem.foundations.recursion

import io.github.mvillafuertem.foundations.recursion.JsonDataStructure._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

final class JsonDataStructureSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  val john: Json = JsonObject(
    Map(
      "name"    -> JsonString(" John Doe "),
      "age"     -> JsonNumber(25),
      "address" -> JsonObject(
        Map(
          "street-number" -> JsonNumber(25),
          "street-name"   -> JsonString("  Cody Road")
        )
      )
    )
  )

  it should "trimAll" in {
    trimAll(john) shouldBe JsonObject(
      Map(
        "name"    -> JsonString("John Doe"),
        "age"     -> JsonNumber(25),
        "address" -> JsonObject(
          Map(
            "street-number" -> JsonNumber(25),
            "street-name"   -> JsonString("Cody Road")
          )
        )
      )
    )
  }

  it should "anonymize" in {
    anonymize(john) shouldBe JsonObject(
      Map(
        "name"    -> JsonString("***"),
        "age"     -> JsonNumber(0),
        "address" -> JsonObject(
          Map(
            "street-number" -> JsonNumber(0),
            "street-name"   -> JsonString("***")
          )
        )
      )
    )

  }

  it should "search" in {
    search(JsonObject(Map.empty), "ll", 5) shouldBe false
    search(JsonNumber(5), "ll", 5) shouldBe false
    search(JsonString("Hello"), "ll", 5) shouldBe true
    search(JsonObject(Map("message" -> JsonString("Hello"))), "ll", 5) shouldBe true
    search(JsonObject(Map("message" -> JsonString("Hello"))), "ss", 5) shouldBe false
    search(JsonObject(Map("message" -> JsonString("hi"))), "ll", 5) shouldBe false

    search(JsonObject(Map("user" -> JsonObject(Map("name" -> JsonString("John"))))), "o", 2) shouldBe true
    search(JsonObject(Map("user" -> JsonObject(Map("name" -> JsonString("John"))))), "o", 1) shouldBe false
  }

  it should "depth" in {
    depth(JsonNumber(1)) shouldBe 0
    depth(JsonObject(Map.empty)) shouldBe 0
    depth(JsonObject(Map("k" -> JsonNumber(1)))) shouldBe 1
    depth(john) shouldBe 2
  }

}
