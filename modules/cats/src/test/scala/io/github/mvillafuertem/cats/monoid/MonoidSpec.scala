package io.github.mvillafuertem.cats.monoid

import org.scalatest.flatspec.AnyFlatSpecLike
import cats.Monoid
import cats.instances.string._
import org.scalatest.matchers.should.Matchers

final class MonoidSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "combine" in {

    val actual = Monoid[String].combine("Hi ", "there")
    actual shouldBe "Hi there"

  }

  it should "empty" in {

    val actual = Monoid[String].empty
    actual shouldBe ""

  }

}
