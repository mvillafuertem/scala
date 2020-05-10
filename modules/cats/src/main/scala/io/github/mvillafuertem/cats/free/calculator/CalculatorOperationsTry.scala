package io.github.mvillafuertem.cats.free.calculator

import cats.~>
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT._

import scala.util.Try

object CalculatorOperationsTry {

  // I N T E R P R E T E R  T R Y
  val compilerTry: CalculatorOperationsADT ~> Try =
    new (CalculatorOperationsADT ~> Try) {
      override def apply[A](fa: CalculatorOperationsADT[A]) =
        fa match {
          case Sum(a, b) => Try(a.asInstanceOf[Int] + b.asInstanceOf[Int]).asInstanceOf[Try[A]]
          case Sub(a, b) => Try(a.asInstanceOf[Int] - b.asInstanceOf[Int]).asInstanceOf[Try[A]]
          case Mul(a, b) => Try(a.asInstanceOf[Int] * b.asInstanceOf[Int]).asInstanceOf[Try[A]]
          case Div(a, b) => Try(a.asInstanceOf[Int] / b.asInstanceOf[Int]).asInstanceOf[Try[A]]
        }
    }

  // P R O G R A M  T R Y
  def programTry(a: Int, b: Int): Ops[Int]        =
    for {

      c <- sum[Int](a, b)
      d <- sub[Int](c, b)
      e <- mul[Int](d, a)
      f <- div[Int](e, a)

    } yield f

}
