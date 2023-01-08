package io.github.mvillafuertem.cats.free.calculator

import cats.~>
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT._

object CalculatorOperationsOption {

  // I N T E R P R E T E R  O P T I O N
  val compilerOption: CalculatorOperationsADT ~> Option =
    new (CalculatorOperationsADT ~> Option) {
      override def apply[A](fa: CalculatorOperationsADT[A]): Option[A] =
        fa match {
          case Sum(a, b) => Option(a.asInstanceOf[Int] + b.asInstanceOf[Int]).asInstanceOf[Option[A]]
          case Sub(a, b) => Option(a.asInstanceOf[Int] - b.asInstanceOf[Int]).asInstanceOf[Option[A]]
          case Mul(a, b) => Option(a.asInstanceOf[Int] * b.asInstanceOf[Int]).asInstanceOf[Option[A]]
          case Div(a, b) =>
            if (b == 0)
              None
            else
              Some(a.asInstanceOf[Int] / b.asInstanceOf[Int]).asInstanceOf[Some[A]]
        }
    }

  // P R O G R A M  O P T I O N
  def programOption(a: Int, b: Int): Ops[Int] =
    for {

      c <- sum[Int](a, b)
      d <- sub[Int](c, b)
      e <- mul[Int](d, a)
      f <- div[Int](e, a)

    } yield f

}
