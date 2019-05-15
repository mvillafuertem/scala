package io.github.mvillafuertem.cats.free.calculator

import cats.instances.option.catsStdInstancesForOption
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsOption._
import org.scalatest.{FlatSpec, Matchers}

class CalculatorOperationsOptionSpec extends FlatSpec with Matchers {

  behavior of "Calculator Operations Option"

  it should "compiler option some" in {

    // G I V E N
    val a = 1
    val b = 2

    // W H E N
    val option = programOption(a, b).foldMap(compilerOption)

    // T H E N
    option shouldBe Some(1)
  }

  it should "compiler option none" in {

    // G I V E N
    val a = 0
    val b = 3

    // W H E N
    val option = programOption(a, b).foldMap(compilerOption)

    // T H E N
    option shouldBe None
  }

}
