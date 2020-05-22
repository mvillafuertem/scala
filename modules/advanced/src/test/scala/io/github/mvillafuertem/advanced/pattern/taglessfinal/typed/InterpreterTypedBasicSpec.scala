package io.github.mvillafuertem.advanced.pattern.taglessfinal.typed

import io.github.mvillafuertem.advanced.pattern.taglessfinal.typed.InterpreterTypedBasic.F
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class InterpreterTypedBasicSpec extends AnyFlatSpecLike with Matchers {

  behavior of "InterpreterLanguageTypedBasicSpecSpec"

  it should "interpreter typed basic" in {

    // g i v e n
    val multiplyExpression = LanguageTypedBasic.buildType(2)

    // w h e n
    val actual: F[Int] = multiplyExpression(InterpreterTypedBasic.interpreterExtended)

    // t h e n
    actual shouldBe 2

  }

}
