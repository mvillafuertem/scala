package io.github.mvillafuertem.cats.functor

import cats.Functor
import cats.instances.list._
import cats.instances.option._
import cats.syntax.functor._
import io.github.mvillafuertem.cats.functor.FunctorSpec.{ doMath, func1, func2 }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class FunctorSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${getClass.getSimpleName}"

  it should "composition int using map" in {

    val actual = (func1 map func2)(1)
    actual shouldBe (func1 andThen func2) (1)

  }

  it should "composition list using map" in {

    val list   = List(1, 2, 3)
    val actual = Functor[List].map(list)(_ * 2)
    actual shouldBe list.map(_ * 2)

  }

  it should "using map with tagless final pattern" in {

    val actual = doMath(Option(20))
    actual shouldBe Some(22)

  }

}

object FunctorSpec {

  val func1: Int => Double = (x: Int) => x.toDouble

  val func2: Double => Double = (y: Double) => y * 2

  def doMath[F[_]: Functor](start: F[Int]): F[Int] =
    start.map(n => n + 1 * 2)

}
