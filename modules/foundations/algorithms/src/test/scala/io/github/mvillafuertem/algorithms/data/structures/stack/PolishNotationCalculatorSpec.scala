package io.github.mvillafuertem.algorithms.data.structures.stack

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.annotation.tailrec
import scala.collection.mutable

class PolishNotationCalculatorSpec extends AnyFlatSpecLike with Matchers {

  /**
   * // Assume valid expressions as in only contains operators, operands and spaces as delimiter
   *
   * def calculate(expression: String): Int = ???
   *
   * They gave use these examples:
   *
   * PN: + 2 4
   * Equivalent in Infix: 2 + 4
   * Result: 6
   *
   * Assertion: assert(calculate("+ 2 4") == 6)
   *
   * PN:  * - 5 6 7
   * Equivalent in Infix: (5 − 6) * 7
   * Result: -7
   *
   * Assertion: assert(calculate("* - 5 6 7") == -7)
   *
   * PN: - - 2021 1987 - 2021 1994
   * Equivalent in Infix: (2021 - 1987) - (2021 - 1994)
   * Result: 7
   *
   * Assertion: assert(calculate("- - 2021 1987 - 2021 1994") == 7)
   *
   */
  behavior of "Polish Notation Calculator"

  "prefix operators" should "pass using Stack Data Structure" in {
    assert(calculateUsingStack("+ 2 4") == 6)
    assert(calculateUsingStack("* - 5 6 7") == -7)
    assert(calculateUsingStack("- - 2021 1987 - 2021 1994") == 7)
  }

  "prefix operators" should "pass using Recursive function" in {
    assert(calculateUsingRecursiveFunction("+ 2 4") == 6)
    assert(calculateUsingRecursiveFunction("* - 5 6 7") == -7)
    assert(calculateUsingRecursiveFunction("- - 2021 1987 - 2021 1994") == 7)
  }

  def calculateUsingStack(expression: String): Int = {
    val expr = expression
      .split(" ")
      .toList
      .reverse // delete this for postfix operators

    val stack = mutable.Stack[Int]()
    expr.foreach {
      case "+" => stack.push(stack.pop() + stack.pop())
      case "-" => stack.push(stack.pop() - stack.pop())
      case "*" => stack.push(stack.pop() * stack.pop())
      case number => stack.push(number.toInt)
    }
    stack.pop()
  }

  def calculateUsingRecursiveFunction(expression: String): Int = {
    val expr = expression
      .split(" ")
      .toList
      .reverse

    @tailrec
    def _recursive(list: List[String], accNumber: List[Int]): Int = {
      (list, accNumber) match {
        case (::("+", next), ::(number1, ::(number2, tail))) => _recursive(next, number1 + number2 :: tail )
        case (::("-", next), ::(number1, ::(number2, tail))) => _recursive(next, number1 - number2 :: tail )
        case (::("*", next), ::(number1, ::(number2, tail))) => _recursive(next, number1 * number2 :: tail )
        case (::(number, next), _) => _recursive(next, number.toInt :: accNumber)
        case (Nil, _) => accNumber.head
      }
    }
    _recursive(expr, Nil)

  }

}
