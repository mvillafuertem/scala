package io.github.mvillafuertem.foundations

// https://www.youtube.com/watch?v=L0aYcq1tqMo
trait Applicative[F[_]] extends Apply[F] {

  def zip[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  def pure[A](a: A): F[A]

}
object Applicative {

  def apply[F[_]: Applicative]: Applicative[F] = implicitly

  given intListApplicative: Applicative[List] with {
    def zip[A, B](fa: List[A], fb: List[B]): List[(A, B)] = fa.zip(fb)
    def pure[A](a: A): List[A]                            = List(a)
    def map[A, B](a: List[A])(f: A => B): List[B]         = a.map(f)
    def ap[A, B](fa: List[A])(ff: List[A => B]): List[B]  = ???
  }

  def test(): Unit = {

    def zipX[F[_]: Applicative](a: F[Int], b: F[Int]): F[(Int, Int)] =
      Applicative[F].zip(a, b)

    println(zipX(List(1, 2), List(2, 1)))

  }

}
