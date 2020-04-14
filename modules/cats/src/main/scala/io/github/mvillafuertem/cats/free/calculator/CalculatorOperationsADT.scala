package io.github.mvillafuertem.cats.free.calculator

import cats.free.Free
import cats.free.Free.liftF

// A L G E B R A
sealed trait CalculatorOperationsADT[A]

object CalculatorOperationsADT {

  case class Sum[A](a: A, b: A) extends CalculatorOperationsADT[A]

  case class Sub[A](a: A, b: A) extends CalculatorOperationsADT[A]

  case class Mul[A](a: A, b: A) extends CalculatorOperationsADT[A]

  case class Div[A](a: A, b: A) extends CalculatorOperationsADT[A]

  // https://typelevel.org/cats/datatypes/freemonad.html
  // A L I A S  M O N A D  TYPE
  type Ops[A] = Free[CalculatorOperationsADT, A]

  // O P E R A T I O N S  U S I N G  S M A R T  C O N S T R U C T O R
  def sum[A](a: A, b: A) = liftF[CalculatorOperationsADT, A](Sum[A](a, b))

  def sub[A](a: A, b: A) = liftF[CalculatorOperationsADT, A](Sub[A](a, b))

  def mul[A](a: A, b: A) = liftF[CalculatorOperationsADT, A](Mul[A](a, b))

  def div[A](a: A, b: A) = liftF[CalculatorOperationsADT, A](Div[A](a, b))

}
