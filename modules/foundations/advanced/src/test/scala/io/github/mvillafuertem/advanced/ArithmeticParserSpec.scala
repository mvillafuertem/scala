package io.github.mvillafuertem.advanced

import io.github.mvillafuertem.advanced.ArithmeticParserSpec.{ isOdd, printEval, ArithmeticParser, IntLeaf, Sum }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.annotation.tailrec
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

final class ArithmeticParserSpec extends AnyFlatSpec with Matchers {

  behavior of s"${this.getClass.getSimpleName}"

  it should "Sum" in {

    val expresion = "((1 + 7) + ((3 + 9) + 5))"

    val actual = printEval(ArithmeticParser(expresion))

    actual shouldBe 25

  }

  it should "Dec" in {

    val expresion = "((1 - 7) + ((3 + 9) + 5))"

    val actual = printEval(ArithmeticParser(expresion))

    actual shouldBe 11

  }

  it should "Check Result" in {

    val expresion = "((1 - 7) + ((3 + 9) + 5))"

    val exp    = Sum(Sum(IntLeaf(1), IntLeaf(7)), Sum(Sum(IntLeaf(3), IntLeaf(9)), IntLeaf(5)))
    val actual = printEval(exp)

    actual shouldBe 25

    isOdd(actual)

  }

}

object ArithmeticParserSpec {

  trait Expr {
    def eval: Int
  }

  case class IntLeaf(n: Int) extends Expr {
    def eval: Int                 = n
    override def toString: String = "%d".format(n)
  }

  case class Sum(a: Expr, b: Expr) extends Expr {
    def eval: Int                 = a.eval + b.eval
    override def toString: String = "(%s + %s)".format(a, b)
  }

  case class Dec(a: Expr, b: Expr) extends Expr {
    def eval: Int                 = a.eval - b.eval
    override def toString: String = "(%s + %s)".format(a, b)
  }

  def combineLeaves(e: Expr): Expr =
    e match {
      case IntLeaf(n)                  => IntLeaf(n)
      case Sum(IntLeaf(a), IntLeaf(b)) => IntLeaf(a + b)
      case Dec(IntLeaf(a), IntLeaf(b)) => IntLeaf(a - b)
      case Sum(a, b)                   => Sum(combineLeaves(a), combineLeaves(b))
      case Dec(a, b)                   => Dec(combineLeaves(a), combineLeaves(b))
    }

  @tailrec
  def printEval(e: Expr): Int = {
    println(e)
    e match {
      case IntLeaf(n) => n
      case _          => printEval(combineLeaves(e))
    }
  }

  val isOdd: PartialFunction[Int, String] = { case x if x % 2 == 1 => x + " is odd" }

  object ArithmeticParser extends RegexParsers {
    private def int: Parser[IntLeaf]                = regex(new Regex("""\d+""")).map(s => IntLeaf(s.toInt))
    private def sum: Parser[Sum]                    = ("(" ~> expr ~ "+" ~ expr <~ ")").map { case (a ~ _ ~ b) => Sum(a, b) }
    private def dec: Parser[Dec]                    = ("(" ~> expr ~ "-" ~ expr <~ ")").map { case (a ~ _ ~ b) => Dec(a, b) }
    private def expr: ArithmeticParser.Parser[Expr] = int | sum | dec
    def parse(str: String): ParseResult[Expr]       = parseAll(expr, str)
    def apply(str: String): Expr                    =
      ArithmeticParser.parse(str) match {
        case ArithmeticParser.Success(result: Expr, _) => result
        case _                                         => sys.error("Could not parse the input string: " + str)
      }

  }

}
