package io.github.mvillafuertem.cats.foldable

import cats._
import cats.kernel.Monoid
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class FoldableSpec extends AnyFlatSpecLike with Matchers {

  it should "WIP" in {

    sealed trait BackwardsList[+A]
    case object BWLNil extends BackwardsList[Nothing]
    case class BWLCons[A](last: A, init: BackwardsList[A])

    val actual: Int = Foldable[List].foldLeft(List(1, 2, 3), 1)(_ * _)

    val monoid = Foldable[List].fold(List(1, 2, 3))(Monoid[Int])

    val actualMap = Foldable[List].foldMap(List(1, 2, 3))(identity)(Monoid[Int])



    actual shouldBe 6
    monoid shouldBe 6
    actualMap shouldBe 6

  }

}
