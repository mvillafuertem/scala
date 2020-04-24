package io.github.mvillafuertem.cats.eitherk

import cats.data.EitherK
import cats.free.Free
import cats.{ ~>, Id }
import io.github.mvillafuertem.algorithms.data.structures.stack.Stack
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT.{ Sub, Sum }
import io.github.mvillafuertem.cats.free.data.structures.StackADT
import io.github.mvillafuertem.cats.free.data.structures.StackADT.{ Peek, Pop, Push, Show }

object EitherKApplication extends App {

  type CalculatorApp[A] = EitherK[CalculatorOperationsADT, StackADT, A]

  def program(implicit I: CalculatorOperations[CalculatorApp], D: StackRepository[CalculatorApp]): Free[CalculatorApp, String] = {

    import D._
    import I._

    for {
      result <- sum(1, 2)
      saved  <- addResultOperation(result)
    } yield saved.show()
  }

  object ConsoleCatsInterpreter extends (CalculatorOperationsADT ~> Id) {
    def apply[A](fa: CalculatorOperationsADT[A]) = fa match {
      case Sum(a, b)                         => (a.asInstanceOf[Int] + b.asInstanceOf[Int]).asInstanceOf[A]
      case Sub(a, b)                         => (a.asInstanceOf[Int] - b.asInstanceOf[Int]).asInstanceOf[A]
      case CalculatorOperationsADT.Mul(a, b) => (a.asInstanceOf[Int] * b.asInstanceOf[Int]).asInstanceOf[A]
      case CalculatorOperationsADT.Div(a, b) => (a.asInstanceOf[Int] / b.asInstanceOf[Int]).asInstanceOf[A]
    }
  }

  object InMemoryDatasourceInterpreter extends (StackADT ~> Id) {

    private[this] val stack = new Stack[Int]

    def apply[A](fa: StackADT[A]) = fa match {
      case Push(a) => stack.push(a.asInstanceOf[Int]).asInstanceOf[A]
      case Show()  => stack.show().asInstanceOf[A]
      // TODO
      case Pop()  => stack.show().asInstanceOf[A]
      case Peek() => stack.show().asInstanceOf[A]
    }
  }

  val interpreter: CalculatorApp ~> Id = ConsoleCatsInterpreter or InMemoryDatasourceInterpreter

  import CalculatorOperations._
  import StackRepository._

  private val value: Id[String] = program.foldMap(interpreter)

  println(value)
}
