package io.github.mvillafuertem.foundations

import scala.util.Try

trait Functor[F[_]] {
  def map[A, B](a: F[A])(f: A => B): F[B]
}

object Functor {
  
  def apply[F[_] : Functor] : Functor[F] = implicitly

  given intFunctor: Functor[List] with {
    def map[A, B](a: List[A])(f: A => B): List[B] = a.map(f)
  }

  // Problema: Código repetitivo para cada estructura de dato
  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def do10xTry(aTry: Try[Int]): Try[Int] = aTry.map(_ * 10)

  // Solución: Generalizar para cualquier estructura de dato usando Functor
  def do10x[F[_]: Functor](a: F[Int]): F[Int] =
    Functor[F].map(a)(_ * 10)
    
  def test(): Unit = {
    val resultList =  do10x(List(1,2,3))
    
    println(resultList)
    
    // Definimos una nueva estructura de datos 
    // y sigue funcionando el método do10X 😱
    sealed trait Tree[+T]
    object Tree {
      def leaf[T](value: T): Tree[T] = Leaf(value)
      def branch[T](value: T, left: Tree[T], right: Tree[T]): Tree[T] = Branch(value, left, right)
    }
    case class Leaf[+T](value: T) extends Tree[T]
    case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

    given treeFunctor: Functor[Tree] with {
      def map[A, B](a: Tree[A])(f: A => B): Tree[B] = a match {
        case Leaf(value) => Leaf(f(value))
        case Branch(value, left, right) => Branch(f(value), map(left)(f), map(right)(f))
      }
    }
    
    val tree: Tree[Int] =
      Tree.branch(1, 
        Tree.branch(2, 
          Tree.leaf(3), 
          Tree.leaf(4)
        ), 
        Tree.leaf(5)
      )

    val resultTree = do10x(tree)

    println(resultTree)
    
    extension [F[_]: Functor, A, B](a: F[A])
      def map(f: A => B) = Functor[F].map(a)(f)

    println(tree.map(_ * 10)) // ahora tree contiene el operador map 😱
  }

}
