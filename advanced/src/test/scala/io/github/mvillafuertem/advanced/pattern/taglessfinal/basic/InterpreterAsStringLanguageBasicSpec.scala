package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import io.github.mvillafuertem.advanced.pattern.taglessfinal.basic.InterpreterAsStringLanguageBasic.F
import org.scalatest.{FlatSpecLike, Matchers}

class InterpreterAsStringLanguageBasicSpec extends FlatSpecLike with Matchers {

  behavior of "InterpreterAsStringLanguageBasicSpec"

  it should "interpreted basic as string" in {

    // g i v e n
    val increment = LanguageBasic.buildIncrementNumber(0)
    val basicExpression = LanguageBasic.buildIncrementExpression(increment)

    // w h e n
    val actual: F[Int] = basicExpression(InterpreterAsStringLanguageBasic.interpreterAsString)

    // t h e n
    actual shouldBe "(inc (inc (0)))"

  }

  it should "interpreted full as string" in {

    // g i v e n
    val fullExpression = LanguageBasic.buildComplexExpression("Result is", 10, 1)

    // w h e n
    val actual: F[String] = fullExpression(InterpreterAsStringLanguageBasic.interpreterAsString)

    // t h e n
    actual shouldBe "(concat [Result is] (toString (+ (10) (inc (1)))))"

  }

}
