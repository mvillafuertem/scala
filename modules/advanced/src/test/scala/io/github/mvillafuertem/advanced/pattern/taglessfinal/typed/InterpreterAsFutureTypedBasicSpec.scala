package io.github.mvillafuertem.advanced.pattern.taglessfinal.typed

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class InterpreterAsFutureTypedBasicSpec extends AsyncFlatSpecLike with Matchers {

  behavior of "InterpreterAsFutureTypedBasicSpec"

  it should "interpreted basic typed as future" in {

    // g i v e n
    val multiplyExpression = LanguageTypedBasic.buildType(2)

    // w h e n
    val actual: Future[Int] = multiplyExpression(InterpreterAsFutureTypedBasic.interpreterAsFuture)

    // t h e n
    actual.map(_ shouldBe 2)

  }

}
