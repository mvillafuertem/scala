package io.github.mvillafuertem.algorithms.data.structures.stack

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class StackSpec extends AnyFlatSpec with Matchers {

  behavior of "Stack Spec"

  it should "show" in {

    // G I V E N
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)

    // W H E N
    val actual = stack.show()

    // T H E N
    val expected = "2\n1\n"

    actual shouldBe expected

  }

  it should "length" in {

    // G I V E N
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)

    // W H E N
    val actual = stack.length

    // T H E N
    actual shouldBe 2

  }

  it should "isEmpty" in {

    // G I V E N
    val stack = new Stack[Int]

    // W H E N
    val actual = stack.isEmpty

    // T H E N
    actual shouldBe true

  }

  it should "push" in {

    // G I V E N
    val stack = new Stack[Int]

    // W H E N
    val actual = stack.push(1)

    // T H E N
    actual.isEmpty shouldBe false
    actual.length shouldBe 1

  }

  ignore should "peek" in {

    // G I V E N
    val stack = new Stack[Int]
    stack.push(1)

    // W H E N
    val actual = stack.peek

    // T H E N
    actual shouldBe 1

  }

  ignore should "pop" in {

    // G I V E N
    val stack = new Stack[Int]
    stack.push(1)

    // W H E N
    val actual = stack.pop

    // T H E N
    actual shouldBe 1

  }

}
