package io.github.mvillafuertem.advanced.implicits

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

class ConditionalImplicitValuesSpec extends AnyFlatSpecLike with Matchers {

  behavior of "ConditionalImplicitValuesSpec"

  it should "conditional implicit from execution context, see the signature of method run" in {

    // g i v e n
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    // w h e n
    val actual = ConditionalImplicitValues().n1(1).n2(2).run() // (ConditionalImplicit.conditionalImplicitFromExecutionContext(ec))

    // t h e n
    actual shouldBe 3

  }

}
