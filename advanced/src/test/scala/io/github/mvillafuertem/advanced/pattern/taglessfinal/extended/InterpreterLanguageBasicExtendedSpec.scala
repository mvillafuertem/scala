package io.github.mvillafuertem.advanced.pattern.taglessfinal.extended

import io.github.mvillafuertem.advanced.pattern.taglessfinal.extended.InterpreterLanguageBasicExtended.F
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class InterpreterLanguageBasicExtendedSpec extends AnyFlatSpecLike with Matchers {

  behavior of "InterpreterLanguageBasicExtendedSpec"

  it should "interpreterExtended" in {

    // g i v e n
    val multiplyExpression = LanguageBasicExtended.multiply(2,3)

    // w h e n
    val actual: F[Int] = multiplyExpression(InterpreterLanguageBasicExtended.interpreterExtended)

    // t h e n
    actual shouldBe 6

  }

}
