package io.github.mvillafuertem.cats.semigroup

import cats.{ Monoid, Semigroup }
import cats.instances.string._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import cats.syntax.semigroup._

final class SemigroupSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "combine" in {

    val actual = Semigroup[String].combine("Hi ", "there")
    actual shouldBe Monoid[String].combine("Hi ", "there")

  }

  it should "|+|" in {

    val actual = "Hi " |+| "there" |+| Monoid[String].empty
    actual shouldBe Semigroup[String].combine("Hi ", "there")

  }

}
