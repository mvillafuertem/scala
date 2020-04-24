package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object InterpreterAsFutureLanguageBasic {

  type F[ScalaValue] = Future[ScalaValue]

  val interpreterAsFuture: LanguageBasic[F] = new LanguageBasic[F] {
    override def number(v: Int): F[Int] = Future.successful(v)
    override def increment(a: F[Int]): F[Int] =
      for {
        av <- a
      } yield av + 1
    override def add(a: F[Int], b: F[Int]): F[Int] =
      for {
        av <- a
        bv <- b
      } yield av + bv

    override def text(v: String): F[String] = Future.successful(v)
    override def toUpper(a: F[String]): F[String] =
      for {
        av <- a
      } yield av.toUpperCase

    override def concat(a: F[String], b: F[String]): F[String] =
      for {
        av <- a
        bv <- b
      } yield av + bv

    override def toString(v: F[Int]): F[String] = v.map(_.toString)
  }

}
