package io.github.mvillafuertem.cats.free.data.structures

import cats.free.Free
import cats.free.Free._
import io.github.mvillafuertem.algorithms.data.structures.stack.Stack

// A L G E B R A
sealed trait StackADT[A]

object StackADT {

  case class Push[A](a: A) extends StackADT[Stack[A]]

  case class Pop() extends StackADT[Boolean]

  case class Peek[A]() extends StackADT[Stack[A]]

  case class Show[A]() extends StackADT[Stack[A]]

  // https://typelevel.org/cats/datatypes/freemonad.html
  // A L I A S  M O N A D  TYPE
  type StackFree[A] = Free[StackADT, A]

  // O P E R A T I O N S  U S I N G  S M A R T  C O N S T R U C T O R
  def push[A](a: A) = liftF[StackADT, Stack[A]](Push[A](a))

  def pop() = liftF[StackADT, Boolean](Pop())

  def peek[A]() = liftF[StackADT, Stack[A]](Peek[A]())

  def show[A]() = liftF[StackADT, Stack[A]](Show[A]())

}
