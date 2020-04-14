package io.github.mvillafuertem.cats.eitherk

import cats.InjectK
import cats.free.Free
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT
import io.github.mvillafuertem.cats.free.calculator.CalculatorOperationsADT._

class CalculatorOperations[F[_]](implicit I: InjectK[CalculatorOperationsADT, F]) {

  def sum(a: Int, b: Int): Free[F, Int] = Free.inject[CalculatorOperationsADT, F](Sum(a, b))

  def sub(a: Int, b: Int): Free[F, Int] = Free.inject[CalculatorOperationsADT, F](Sub(a, b))

}

object CalculatorOperations {

  implicit def interacts[F[_]](implicit I: InjectK[CalculatorOperationsADT, F]): CalculatorOperations[F] = new CalculatorOperations[F]

}
