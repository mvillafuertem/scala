package io.github.mvillafuertem.cats.free.data.structures

import cats.instances.option.catsStdInstancesForOption
import io.github.mvillafuertem.algorithms.data.structures.stack.Stack
import io.github.mvillafuertem.cats.free.data.structures.StackOption.{ compilerOption, programOption }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StackOptionSpec extends AnyFlatSpec with Matchers {

  behavior of "Stack Option"

  it should "stack option" in {

    // G I V E N
    val a = 1

    // W H E N
    val option = programOption(a).foldMap(compilerOption)

    // T H E N
    val expected = new Stack[Int]()
    expected.push(1)

    option shouldBe Some(expected)

  }

}
