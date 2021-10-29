package io.github.mvillafuertem.advanced.implicits

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

final class ImplicitsForContextParametersSpec extends AsyncFlatSpecLike with Matchers {

  behavior of "ImplicitsForContextParametersSpec"

  it should "implicit are not available" in {

    // g i v e n
    // w h e n
    val eventualBoolean: Future[Boolean] = ImplicitsForContextParameters.fooWithout(1)(ExecutionContext.global)

    // t h e n
    eventualBoolean.map(_ shouldBe true)

  }

  it should "implicit are available" in {

    // g i v e n
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    // w h e n
    val eventualBoolean: Future[Boolean] = ImplicitsForContextParameters.fooWith(1)

    // t h e n
    eventualBoolean.map(_ shouldBe true)

  }

}
