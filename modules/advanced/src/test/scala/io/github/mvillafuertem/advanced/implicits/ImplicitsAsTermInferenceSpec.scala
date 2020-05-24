package io.github.mvillafuertem.advanced.implicits

import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

final class ImplicitsAsTermInferenceSpec extends AsyncFlatSpecLike with Matchers {

  behavior of "ImplicitsAsTermInferenceSpec"

  it should "without terms inference and types inference" in {

    // g i v e n
    // w h e n
    val eventualUnit = ImplicitsAsTermInference[String]("Hello")(ExecutionContext.global).map(println)

    // t h e n
    eventualUnit.map(_ shouldBe ())

  }

  it should "with terms inference and types inference" in {

    // g i v e n
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    // w h e n
    val eventualUnit = ImplicitsAsTermInference("Hello").map(println)

    // t h e n
    eventualUnit.map(_ shouldBe ())

  }

}
