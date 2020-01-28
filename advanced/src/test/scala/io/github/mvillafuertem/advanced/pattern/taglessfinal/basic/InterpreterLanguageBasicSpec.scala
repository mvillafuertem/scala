package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import io.github.mvillafuertem.advanced.pattern.taglessfinal.basic.InterpreterLanguageBasic.F
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class InterpreterLanguageBasicSpec extends AnyFlatSpecLike with Matchers {

  behavior of "InterpreterLanguageBasicSpec"

  it should "interpreted basic" in {

    // g i v e n
    val increment = LanguageBasic.buildIncrementNumber(0)
    val basicExpression = LanguageBasic.buildIncrementExpression(increment)

    // w h e n
    val actual: F[Int] = basicExpression(InterpreterLanguageBasic.interpreter)

    // t h e n
    actual shouldBe 2

  }

  it should "interpreted full" in {

    // g i v e n
    val fullExpression = LanguageBasic.buildComplexExpression("Result is", 10, 1)

    // w h e n
    val actual: F[String] = fullExpression.apply(InterpreterLanguageBasic.interpreter)

    // t h e n
    actual shouldBe "Result is 12"

  }

}
