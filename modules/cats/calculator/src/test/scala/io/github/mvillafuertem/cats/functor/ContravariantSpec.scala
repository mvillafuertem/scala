package io.github.mvillafuertem.cats.functor

import cats.{ Contravariant, Show }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class ContravariantSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "contramap" in {

    val show = Show[String]

    val actual = Contravariant[Show]
      .contramap(show)((sym: Symbol) => s"'${sym.name}'")

    actual.show(Symbol("Pepe")) shouldBe "'Pepe'"

  }

}
