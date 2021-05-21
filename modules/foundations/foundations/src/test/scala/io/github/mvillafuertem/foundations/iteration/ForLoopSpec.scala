package io.github.mvillafuertem.foundations.iteration

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

final class ForLoopSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  it should "sum" in {
    ForLoop.sum(List(1, 5, 2)) shouldBe 8
    ForLoop.sum(Nil) shouldBe 0
  }

  it should "sum is consistent with List sum" in {
    forAll { (numbers: List[Int]) =>
      ForLoop.sum(numbers) shouldBe numbers.sum
    }
  }

  it should "size" in {
    ForLoop.size(List(2, 5, 1, 8)) shouldBe 4
    ForLoop.size(Nil) shouldBe 0
  }

  it should "size and concat" in {
    forAll { (list1: List[Int], list2: List[Int]) =>
      val size1 = ForLoop.size(list1)
      val size2 = ForLoop.size(list2)

      size1 + size2 shouldBe (list1 ++ list2).size
    }
  }

  it should "min" in {
    ForLoop.min(List(2, 5, 1, 8)) shouldBe Some(1)
    ForLoop.min(Nil) shouldBe None
  }

  it should "min returns a value smaller than all elements in the input list" in {
    forAll { (numbers: List[Int]) =>
      for {
        minValue <- ForLoop.min(numbers)
        number   <- numbers
      } minValue should be <= number
// Otra implementación
//        ForLoop.min(numbers) match {
//        case Some(value) => numbers.foreach(_ should be >= value)
//        case None        => numbers.isEmpty shouldBe true
//      }
    }
  }

  it should "min returns a value that belongs to the input list" in {
    forAll { (numbers: List[Int]) =>
      ForLoop.min(numbers).foreach(minValue => numbers.contains(minValue) shouldBe true)
    }
  }

  it should "wordCount" in {
    ForLoop.wordCount(List("Hi", "Hello", "Hi")) shouldBe Map("Hi" -> 2, "Hello" -> 1)
    ForLoop.wordCount(Nil) shouldBe Map.empty
  }

  it should "wordCount returns frequencies > 0" in {
    forAll { (words: List[String]) =>
      ForLoop.wordCount(words).values.foreach(frequency => frequency should be > 0)
    }
  }

  it should "foldLeft process inputs in orders" in {
    forAll { (numbers: List[Int]) =>
      val result = ForLoop.foldLeft(numbers, List.empty[Int])((state, number) => state :+ number)
      result shouldBe numbers
    }
  }

  it should "foldLeft is consistent with std library" in {
    forAll { (numbers: List[Int], default: Int, combine: (Int, Int) => Int) =>
      ForLoop.foldLeft(numbers, default)(combine) shouldBe numbers.foldLeft(default)(combine)
    }
  }

  it should "foldLeft noop" in {
    forAll { (numbers: List[Int]) =>
      ForLoop.foldLeft(numbers, List.empty[Int])(_ :+ _) shouldBe numbers
    }
  }

}
