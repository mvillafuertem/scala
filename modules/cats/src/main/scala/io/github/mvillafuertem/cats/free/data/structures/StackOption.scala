package io.github.mvillafuertem.cats.free.data.structures

import cats.~>
import io.github.mvillafuertem.algorithms.data.structures.stack.Stack
import io.github.mvillafuertem.cats.free.data.structures.StackADT.{Push, _}

object StackOption {

  // I N T E R P R E T E R  O P T I O N
  val compilerOption: StackADT ~> Option =
    new (StackADT ~> Option) {

      val stack = new Stack[Int]

      override def apply[A](fa: StackADT[A]): Option[A] = fa match {
        case Push(a) =>
          val element = a.asInstanceOf[Int]
          Option(stack.push(element).asInstanceOf[A])
        //case Pop() => stack.pop.asInstanceOf[Option[A]]
        //case Peek() => stack.peek.asInstanceOf[Option[A]]
        //case Show() => stack.show().asInstanceOf[Option[A]]
      }
    }

  // P R O G R A M  O P T I O N
  def programOption(a: Int): StackFree[Stack[Int]] =
    for {

      s <- push[Int](a)
      //_ <- pop()
      //_ <- peek[Int]()
      //f <- show[Int]()

    } yield s
}
