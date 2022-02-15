package io.github.mvillafuertem.mio

import scala.annotation.targetName

/**
 * ¿Qué son las Type Classes? https://dotty.epfl.ch/docs/reference/contextual/type-classes.html ¿Qué son los Rank N Types? ¿Qué es el Context Bound?
 * https://dotty.epfl.ch/docs/reference/contextual/context-bounds.html ¿Qué es una Context Function?
 * https://dotty.epfl.ch/docs/reference/contextual/context-functions.html ¿Qué es Extension? ¿Qué es Infix? ¿Qué es Target Name?
 * https://dotty.epfl.ch/docs/reference/other-new-features/targetName.html ¿Qué es Using Clauses?
 * http://dotty.epfl.ch/docs/reference/contextual/using-clauses.html
 */
object MIO: // extends App:

  trait Functor[F[_]]:
    def fmap[A, B](f: A => B): F[A] => F[B]
    extension [A, B](fa: F[A])
      infix def map(f: A => B): F[B] = fmap(f)(fa)

      @targetName("mapFlipped")
      def <#>(f: A => B): F[B] = fmap(f)(fa)

  object Functor:
    given Functor[Option] with
      override def fmap[A, B](f: A => B): Option[A] => Option[B] =
        (oa: Option[A]) => oa.map(f)

    def mapUsing[F[_]](using functor: Functor[F]) =
      [A, B] => (f: A => B) => (fa: F[A]) => functor.fmap(f)(fa)

    def mapSummon[F[_]](using Functor[F]) =
      [A, B] => (f: A => B) => (fa: F[A]) => summon[Functor[F]].fmap(f)(fa)

    def `map?`[F[_]] =
      [A, B] => (f: A => B) => (fa: F[A]) => (functor: Functor[F]) ?=> functor.fmap(f)(fa)

  trait ApplicativeTypeClassWithHierarchy[F[_]] extends Functor[F]:
    def pure[A](a: A): F[A]
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  trait ApplicativeTypeClassWithContextBound[F[_]: Functor]:
    def pure[A](a: A): F[A]
    def liftA2[A, B, C](f: A => B => C): F[A] => F[B] => F[C]

  def map[F[_]]    = [A, B] => (f: A => B) => (functor: Functor[F]) ?=> (fa: F[A]) => functor.fmap(f)(fa)
  def pure[F[_]]   = [A] => (a: A) => (applicative: ApplicativeTypeClassWithContextBound[F]) ?=> applicative.pure(a)
  def liftA2[F[_]] = [A, B, C] => (f: A => B => C) => (A: ApplicativeTypeClassWithContextBound[F]) ?=> A.liftA2(f)

  given ApplicativeTypeClassWithContextBound[Option] with
    override def pure[A](a: A): Option[A] =
      Option[A](a)

    override def liftA2[A, B, C](f: A => B => C): Option[A] => Option[B] => Option[C] = ???

  map[Option]((a: Int) => a + 1)(Option(1))

  import Functor._
  val fa = pure[Option](1)
  val fb = pure[Option](2)
  val f  = (x: Int) => (y: Int) => x + y

  // val fa = pure[Option](1)
  val ff = (x: Int) => Option(x + 1)

  // assertEquals(flatMap[Option](ff)(fa), Option(2))

  println("Hola")
