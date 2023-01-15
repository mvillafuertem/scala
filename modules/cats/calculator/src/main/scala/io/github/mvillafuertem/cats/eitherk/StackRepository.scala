package io.github.mvillafuertem.cats.eitherk

import cats.InjectK
import cats.free.Free
import io.github.mvillafuertem.algorithms.data.structures.stack.Stack
import io.github.mvillafuertem.cats.free.data.structures.StackADT
import io.github.mvillafuertem.cats.free.data.structures.StackADT.{ Push, Show }

class StackRepository[F[_]](implicit I: InjectK[StackADT, F]) {

  def addResultOperation(a: Int): Free[F, Stack[Int]] = Free.inject[StackADT, F](Push[Int](a))

  def getAllResultsOperation: Free[F, Stack[Int]] = Free.inject[StackADT, F](Show[Int]())

}

object StackRepository {

  implicit def repository[F[_]](implicit I: InjectK[StackADT, F]): StackRepository[F] = new StackRepository[F]

}
