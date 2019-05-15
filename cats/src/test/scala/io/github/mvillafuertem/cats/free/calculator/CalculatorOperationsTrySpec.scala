package io.github.mvillafuertem.cats.free.calculator

import cats.instances.try_.catsStdInstancesForTry
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsTry._
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class CalculatorOperationsTrySpec extends FlatSpec with Matchers {

  behavior of "Calculator Operations Try"

  it should "compiler try" in {

    // G I V E N
    val a = 1
    val b = 2

    // W H E N
    val `try` = programTry(a, b).foldMap(compilerTry)

    // T H E N
    `try` shouldBe Success(1)
  }

}
