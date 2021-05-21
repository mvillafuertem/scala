package io.github.mvillafuertem.foundations

trait Monad[F[_]] extends Applicative[F] with Bind[F] {

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)(a => pure(f(a)))

  def zip[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    flatMap(fa)(a => map(fb)(b => (a, b)))

}

object Monad {}
