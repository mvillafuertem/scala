package io.github.mvillafuertem.foundations

trait Bind[F[_]] extends Apply[F] {

  def >>=[A, B](fa: F[A])(f: A => F[B]): F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]      = >>=(fa)(f)
  def flatten[A](ffa: F[F[A]]): F[A]                   = >>=(ffa)(identity)
  override def ap[A, B](fa: F[A])(ff: F[A => B]): F[B] =
    >>=(ff)(x => map(fa)(x))
}
