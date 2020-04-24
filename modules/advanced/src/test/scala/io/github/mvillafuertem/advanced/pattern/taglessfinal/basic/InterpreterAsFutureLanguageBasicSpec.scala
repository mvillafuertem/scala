package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class InterpreterAsFutureLanguageBasicSpec extends AsyncFlatSpecLike with Matchers {

  behavior of "InterpreterAsFutureLanguageBasicSpec"

  it should "interpreted basic as future" in {

    // g i v e n
    val increment       = LanguageBasic.buildIncrementNumber(0)
    val basicExpression = LanguageBasic.buildIncrementExpression(increment)

    // w h e n
    val actual: Future[Int] = basicExpression(InterpreterAsFutureLanguageBasic.interpreterAsFuture)

    // t h e n
    actual.map(a => a shouldBe 2)

  }

  it should "interpreted full as future" in {

    // g i v e n
    val fullExpression = LanguageBasic.buildComplexExpression("Result is ", 10, 1)

    // w h e n
    val actual: Future[String] = fullExpression(InterpreterAsFutureLanguageBasic.interpreterAsFuture)

    // t h e n
    actual.map(a => a shouldBe "Result is 12")

  }

}
