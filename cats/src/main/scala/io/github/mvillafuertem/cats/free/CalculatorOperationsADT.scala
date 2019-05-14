package io.github.mvillafuertem.cats.free

import cats.free.Free
import cats.free.Free.liftF
import cats.~>

import scala.util.Try


// A L G E B R A
sealed trait CalculatorOperationsADT[A]

object CalculatorOperationsADT extends App {

  case class Sum(a: Int, b: Int) extends CalculatorOperationsADT[Int]
  case class Sub(a: Int, b: Int) extends CalculatorOperationsADT[Int]
  case class Mul(a: Int, b: Int) extends CalculatorOperationsADT[Int]
  case class Div(a: Int, b: Int) extends CalculatorOperationsADT[Int]

  //https://typelevel.org/cats/datatypes/freemonad.html
  // A L I A S  M O N A D  TYPE
  type Ops[A] = Free[CalculatorOperationsADT, A]

  // O P E R A T I O N S  U S I N G  S M A R T  C O N S T R U C T O R
  def sum(a: Int, b: Int): Ops[Int] = liftF(Sum(a, b))
  def sub(a: Int, b: Int): Ops[Int] = liftF(Sub(a, b))
  def mul(a: Int, b: Int): Ops[Int] = liftF(Mul(a, b))
  def div(a: Int, b: Int): Ops[Int] = liftF(Div(a, b))

  // I N T E R P R E T E R  O P T I O N
  val compiler: CalculatorOperationsADT ~> Option = new (CalculatorOperationsADT ~> Option) {
    override def apply[A](fa: CalculatorOperationsADT[A]) = fa match {
      case Sum(a, b) => Option(a + b)
      case Sub(a, b) => Option(a - b)
      case Mul(a, b) => Option(a * b)
      case Div(a, b) =>
        if (b == 0) {
          None
        } else {
          Some(a / b)
        }
    }
  }

  // I N T E R P R E T E R  T R Y
  val compilerTry: CalculatorOperationsADT ~> Try = new (CalculatorOperationsADT ~> Try) {
    override def apply[A](fa: CalculatorOperationsADT[A]) = fa match {
      case Sum(a, b) => Try(a + b)
      case Sub(a, b) => Try(a - b)
      case Mul(a, b) => Try(a * b)
      case Div(a, b) => Try(a / b)
    }
  }

  import cats.instances.option.catsStdInstancesForOption
  import cats.instances.try_.catsStdInstancesForTry

  // P R O G R A M  O P T I O N
  def programOption: Ops[Int] = for {

    c <- sum(3, 1)
    d <- sub(c, 2)
    e <- mul(d, 5)
    f <- div(e, 2)

  } yield f

  println(programOption.foldMap(compiler))

  // P R O G R A M  T R Y
  def programTry: Ops[Int] = for {

    c <- sum(3, 1)
    d <- sub(c, 2)
    e <- mul(d, 5)
    f <- div(e, 0)

  } yield f

  println(programTry.foldMap(compilerTry))
}