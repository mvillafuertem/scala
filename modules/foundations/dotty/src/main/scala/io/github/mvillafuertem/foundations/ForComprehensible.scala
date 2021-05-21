package io.github.mvillafuertem.foundations

//El conjunto completo de métodos soportados
//por las for comprehensions NO comparten un
//super tipo común; cada fragmento generado
//es compilado de manera independiente. Si
//hubiera un trait, se vería aproximadamente así:
trait ForComprehensible[F[_]] {
  def map[A, B](f: A => B): F[B]
  def flatMap[A, B](f: A => F[B]): F[B]
  def withFilter[A](p: A => Boolean): F[A]
  def foreach[A](f: A => Unit): Unit
}
