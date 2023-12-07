package io.github.mvillafuertem.algorithms.sorting.techniques

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random

final class MergeSortFTest extends AnyWordSpecLike with Matchers {

  "BackwardsList" should {

    "empty list" in {
      val left: BackwardsList[Int]  = BackwardsList.empty[Int]
      val right: BackwardsList[Int] = BackwardsList.empty[Int]

      val actual = MergeSortF[BackwardsList].mergeSorted(left, right)
      actual shouldBe  BackwardsList.empty[Int]
    }

    "ordered list" in {
      val left: BackwardsList[Int]  = BackwardsList((1 to 3): _*)
      val right: BackwardsList[Int] =  BackwardsList((1 to 3): _*)

      val actual = MergeSortF[BackwardsList].mergeSorted(left, right)
      actual shouldBe BackwardsList(List(1, 1, 2, 2, 3, 3):_*)

    }

    "list with repeated elements" in {
      val left: BackwardsList[Int] = BackwardsList(List.fill(3)(1): _*)
      val right: BackwardsList[Int] = BackwardsList(List.fill(3)(3): _*)

      val actual = MergeSortF[BackwardsList].mergeSorted(left, right)
      actual shouldBe  BackwardsList(List(1, 1, 1, 3, 3, 3):_*)

    }

    "list with random elements" in {
      val random = new Random()
      val leftRandom = List.fill(1000)(random.nextInt(100))
      val left: BackwardsList[Int] = BackwardsList(leftRandom: _*)
      val rightRandom = List.fill(1000)(random.nextInt(100))
      val right: BackwardsList[Int] =  BackwardsList(rightRandom: _*)

      val actual = MergeSortF[BackwardsList].mergeSorted(left, right)
      actual shouldBe BackwardsList((leftRandom ++ rightRandom).sorted:_*)
    }

    "only elements in left list" in {
      val random = new Random()
      val randomNumbers: List[Int] = List.fill(100)(random.nextInt(100))
      val randomOrderedNumbers = randomNumbers.sorted
      val left: BackwardsList[Int] = BackwardsList(randomNumbers: _*)
      val right: BackwardsList[Int] = BackwardsList.empty[Int]

      val actual = MergeSortF[BackwardsList].mergeSorted(left, right)
      actual shouldBe BackwardsList(randomOrderedNumbers:_*)
    }

  }

  "List" should {

    "empty list" in {
      val left: List[Int]  = List.empty[Int]
      val right: List[Int] = List.empty[Int]

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe List.empty[Int]
    }

    "ordered list" in {
      val left: List[Int]  = List(1, 2, 3)
      val right: List[Int] = List(1, 2, 3)

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe List(1, 1, 2, 2, 3, 3)

    }

    "list with repeated elements" in {
      val left: List[Int] = List(1, 1, 1)
      val right: List[Int] = List(3, 3, 3)

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe List(1, 1, 1, 3, 3, 3)
    }

    "list with random elements" in {
      val random = new Random()
      val left: List[Int] = List.fill(1000000)(random.nextInt(100))
      val right: List[Int] = List.fill(1000000)(random.nextInt(100))

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

    "only elements in left list" in {
      val random = new Random()
      val left: List[Int] = List.fill(100000)(random.nextInt(100))
      val right: List[Int] = List.empty[Int]

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

    "only elements in right list" in {
      val random = new Random()
      val left: List[Int] = List.fill(100000)(random.nextInt(100))
      val right: List[Int] = List.empty[Int]

      val actual = MergeSortF[List].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

  }

  "Vector" should {

    "empty Vector" in {
      val left: Vector[Int]  = Vector.empty[Int]
      val right: Vector[Int] = Vector.empty[Int]

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe Vector.empty[Int]
    }

    "ordered Vector" in {
      val left: Vector[Int]  = Vector(1, 2, 3)
      val right: Vector[Int] = Vector(1, 2, 3)

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe Vector(1, 1, 2, 2, 3, 3)

    }

    "Vector with repeated elements" in {
      val left: Vector[Int] = Vector(1, 1, 1)
      val right: Vector[Int] = Vector(3, 3, 3)

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe Vector(1, 1, 1, 3, 3, 3)

    }

    "Vector with random elements" in {
      val random = new Random()
      val left: Vector[Int] = Vector.fill(1000000)(random.nextInt(100))
      val right: Vector[Int] = Vector.fill(1000000)(random.nextInt(100))

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

    "only elements in left Vector" in {
      val random = new Random()
      val left: Vector[Int] = Vector.fill(10000)(random.nextInt(100))
      val right: Vector[Int] = Vector.empty[Int]

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

    "only elements in right Vector" in {
      val random = new Random()
      val left: Vector[Int] = Vector.fill(100000)(random.nextInt(100))
      val right: Vector[Int] = Vector.empty[Int]

      val actual = MergeSortF[Vector].mergeSorted(left, right)
      actual shouldBe (left ++ right).sorted
    }

  }

}
