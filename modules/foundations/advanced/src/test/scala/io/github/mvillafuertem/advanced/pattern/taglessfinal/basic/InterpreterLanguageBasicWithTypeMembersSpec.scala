package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class InterpreterLanguageBasicWithTypeMembersSpec extends AnyFlatSpecLike with Matchers {

  behavior of "InterpreterLanguageBasicWithTypeMembersSpec"

  it should "interpreted basic" in {

    // g i v e n
    val increment                                                                     = LanguageBasicWithTypeMembers.buildIncrementNumber(0)
    val basicExpression: LanguageBasicWithTypeMembers.ScalaToLanguageBasicBridge[Int] = LanguageBasicWithTypeMembers.buildIncrementExpression(increment)

    // w h e n
    val actual = basicExpression(InterpreterLanguageWithTypeMembersBasic.interpreter)

    // t h e n
    actual shouldBe 2

  }

  it should "interpreted full" in {

    // g i v e n
    val fullExpression = LanguageBasicWithTypeMembers.buildComplexExpression("Result is", 10, 1)

    // w h e n
    val actual: String = fullExpression(InterpreterLanguageWithTypeMembersBasic.interpreter)

    // t h e n
    actual shouldBe "Result is 12"

  }

}
