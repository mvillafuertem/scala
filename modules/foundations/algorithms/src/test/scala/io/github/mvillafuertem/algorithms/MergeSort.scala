package io.github.mvillafuertem.algorithms

import cats.Reducible
import cats.data.NonEmptyList
import io.github.mvillafuertem.algorithms.MergeSort._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.{Duration, Instant}
import scala.annotation.tailrec

final class MergeSort extends AnyWordSpecLike with Matchers {

  "mergeSortedIntLists" should {

    "return an empty List when leftList and rightList are empty" in {
      mergeSortedIntLists(List.empty[Int], List.empty[Int]) shouldBe List.empty[Int]
    }

    "return the same List when leftList contains only one element and rightList is empty" in {
      mergeSortedIntLists(List(1), List.empty[Int]) shouldBe List(1)
    }

    "return the same List when rightList contains only one element and leftList is empty" in {
      mergeSortedIntLists(List.empty[Int], List(1)) shouldBe List(1)
    }

    "return the ordered List when leftList contains elements and rightList is empty" in {
      mergeSortedIntLists(List(13, 8, 5, 3, 2, 1), List.empty[Int]) shouldBe List(1, 2, 3, 5, 8, 13)
    }

    "return the ordered List when rightList contains elements and leftList is empty" in {
      mergeSortedIntLists(List.empty[Int], List(13, 8, 5, 3, 2, 1)) shouldBe List(1, 2, 3, 5, 8, 13)
    }

    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
      mergeSortedIntLists(List(1, 2, 3, 5, 8, 13), List(55, 21, 34)) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
    }

    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
      mergeSortedIntLists(List(55, 21, 34), List(1, 2, 3, 5, 8, 13)) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
    }
  }

  "mergeSortedLists[Int]" should {

    "return an empty List when leftList and rightList are empty" in {
      mergeSortedLists(List.empty[Int], List.empty[Int])(_ < _) shouldBe List.empty[Int]
    }

    "return the same List when leftList contains only one element and rightList is empty" in {
      mergeSortedLists(List(1), List.empty[Int])(_ < _) shouldBe List(1)
    }

    "return the same List when rightList contains only one element and leftList is empty" in {
      mergeSortedLists(List.empty[Int], List(1))(_ < _) shouldBe List(1)
    }

    "return the ordered List when leftList contains elements and rightList is empty" in {
      mergeSortedLists(List(13, 8, 5, 3, 2, 1), List.empty[Int])(_ < _) shouldBe List(1, 2, 3, 5, 8, 13)
    }

    "return the ordered List when rightList contains elements and leftList is empty" in {
      mergeSortedLists(List.empty[Int], List(13, 8, 5, 3, 2, 1))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13)
    }

    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
      mergeSortedLists(List(1, 2, 3, 5, 8, 13), List(55, 21, 34))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
    }

    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
      mergeSortedLists(List(55, 21, 34), List(1, 2, 3, 5, 8, 13))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
    }
  }

  "mergeSortedLists[String]" should {
    "return an empty List when leftList and rightList are empty" in {
      mergeSortedLists(List.empty[String], List.empty[String])(_ < _) shouldBe List.empty[String]
    }

    "return the same List when leftList contains only one element and rightList is empty" in {
      mergeSortedLists(List("a"), List.empty[String])(_ < _) shouldBe List("a")
    }

    "return the same List when rightList contains only one element and leftList is empty" in {
      mergeSortedLists(List.empty[String], List("a"))(_ < _) shouldBe List("a")
    }

    "return the ordered List when leftList contains elements and rightList is empty" in {
      mergeSortedLists(List("f", "e", "d", "c", "b", "a"), List.empty[String])(_ < _) shouldBe List("a", "b", "c", "d", "e", "f")
    }

    "return the ordered List when rightList contains elements and leftList is empty" in {
      mergeSortedLists(List.empty[String], List("f", "e", "d", "c", "b", "a"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f")
    }

    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
      mergeSortedLists(List("a", "b", "c", "d", "e", "f"), List("i", "h", "g"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f", "g", "h", "i")
    }

    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
      mergeSortedLists(List("i", "h", "g"), List("a", "b", "c", "d", "e", "f"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f", "g", "h", "i")
    }
  }

  "mergeSorted[BWLCons, Int]" should {
    "return the ordered List when leftList contains elements and rightList is empty" in {
      val ordering = Ordering.fromLessThan[Int](_ < _)
      mergeSorted[Int](BWLCons(3, BWLCons(1, BWLCons(2, BWLNil))), BWLCons(8, BWLCons(5, BWLCons(13, BWLNil))))(ordering) shouldBe
        BWLCons(1, BWLCons(2, BWLCons(3, BWLCons(5, BWLCons(8, BWLCons(13, BWLNil))))))
    }

  }

}

object MergeSort {

  def mergeSortedIntLists(left: List[Int], right: List[Int]): List[Int] = {

    def loop(list: List[Int]): List[Int] =
      list match {
        case Nil => Nil
        case head :: Nil => List(head)
        case _ =>
          val (left, right) = list.splitAt(list.length / 2)
          inner(loop(left), loop(right))
      }

    def inner(left: List[Int], right: List[Int]): List[Int] = (left, right) match {
      case (Nil, _)                                       => loop(right)
      case (_, Nil)                                       => loop(left)
      case (headLeft :: tailLeft, headRight :: tailRight) =>
        if (headLeft < headRight) headLeft :: inner(tailLeft, right)
        else headRight :: inner(left, tailRight)
    }

    val start = Instant.now()
    val value = inner(left, right)
    val end = Instant.now()
    println("mergeSortedIntLists: " + Duration.between(start, end))
    value
  }

  def mergeSortedLists[A](left: List[A], right: List[A])(implicit lessThan: (A, A) => Boolean): List[A] = {

    def loop(list: List[A])(implicit lessThan: (A, A) => Boolean): List[A] =
      list match {
        case Nil => Nil
        case head :: Nil => List(head)
        case _ =>
          val (left, right) = list.splitAt(list.length / 2)
          inner(loop(left), loop(right))
      }

    def inner(left: List[A], right: List[A]): List[A] = (left, right) match {
      case (Nil, _)                                       => loop(right)
      case (_, Nil)                                       => loop(left)
      case (headLeft :: tailLeft, headRight :: tailRight) =>
        if (lessThan(headLeft, headRight)) headLeft :: inner(tailLeft, right)
        else headRight :: inner(left, tailRight)
    }

    val start = Instant.now()
    val value = inner(left, right)
    val end   = Instant.now()
    println("mergeSortedLists: " + Duration.between(start, end))
    value
  }

  sealed trait BackwardsList[+A] {
    def head: A
    def tail: BackwardsList[A]
    def isEmpty: Boolean
    def reverse: BackwardsList[A]
    def ::[S >: A](elem: S): BackwardsList[S] = BWLCons(elem, this)
    def ++[S >: A](anotherList: BackwardsList[S]): BackwardsList[S]
    def flatMap[S](f: A => BackwardsList[S]): BackwardsList[S]
    def map[S](f: A => S): BackwardsList[S]

  }

  case object BWLNil extends BackwardsList[Nothing] {
    override def isEmpty: Boolean = true

    override def head: Nothing = throw new NoSuchElementException

    override def tail: BackwardsList[Nothing] = throw new NoSuchElementException

    override def reverse: BackwardsList[Nothing] = BWLNil

    override def ++[S >: Nothing](anotherList: BackwardsList[S]): BackwardsList[S] = anotherList

    override def flatMap[S](f: Nothing => BackwardsList[S]): BackwardsList[S] = BWLNil

    override def map[S](f: Nothing => S): BackwardsList[S] = ???
  }

  case class BWLCons[A](head: A, tail: BackwardsList[A]) extends BackwardsList[A] {
    override def isEmpty: Boolean = false

    override def reverse: BackwardsList[A] = {
      @tailrec
      def reverseTailrec(remainingList: BackwardsList[A], result: BackwardsList[A]): BackwardsList[A] =
        if (remainingList.isEmpty) result
        else reverseTailrec(remainingList.tail, remainingList.head :: result)

      reverseTailrec(this, BWLNil)
    }

    override def ++[S >: A](anotherList: BackwardsList[S]): BackwardsList[S] = {
      @tailrec
      def concatTailrec(remainingList: BackwardsList[S], acc: BackwardsList[S]): BackwardsList[S] =
        if (remainingList.isEmpty) acc
        else concatTailrec(remainingList.tail, remainingList.head :: acc)

      concatTailrec(anotherList, this.reverse).reverse
    }

    override def flatMap[S](f: A => BackwardsList[S]): BackwardsList[S] = {

      @tailrec
      def betterFlatMap(remaining: BackwardsList[A], accumulator: BackwardsList[BackwardsList[S]]): BackwardsList[S] =
        if (remaining.isEmpty) concatenateAll(accumulator, BWLNil, BWLNil)
        else betterFlatMap(remaining.tail, f(remaining.head).reverse :: accumulator)

      @tailrec
      def concatenateAll(elements: BackwardsList[BackwardsList[S]], currentList: BackwardsList[S], accumulator: BackwardsList[S]): BackwardsList[S] =
        if (currentList.isEmpty && elements.isEmpty) accumulator
        else if (currentList.isEmpty) concatenateAll(elements.tail, elements.head, accumulator)
        else concatenateAll(elements, currentList.tail, currentList.head :: accumulator)

      betterFlatMap(this, BWLNil)

    }

    override def map[S](f: A => S): BackwardsList[S] = {
      @tailrec
      def mapTailrec(remaining: BackwardsList[A], accumulator: BackwardsList[S]): BackwardsList[S] =
        if (remaining.isEmpty) accumulator.reverse
        else mapTailrec(remaining.tail, f(remaining.head) :: accumulator)

      mapTailrec(this, BWLNil)
    }
  }

  def mergeSorted[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ordering: Ordering[A]): BackwardsList[A] = {

    @tailrec
    def merge(listA: BackwardsList[A], listB: BackwardsList[A], accumulator: BackwardsList[A]): BackwardsList[A] =
      if (listA.isEmpty) accumulator.reverse ++ listB
      else if (listB.isEmpty) accumulator.reverse ++ listA
      else if (ordering.lteq(listA.head, listB.head)) merge(listA.tail, listB, listA.head :: accumulator)
      else merge(listA, listB.tail, listB.head :: accumulator)

    @tailrec
    def mergeSortTailrec(smallLists: BackwardsList[BackwardsList[A]], bigLists: BackwardsList[BackwardsList[A]]): BackwardsList[A] =
      if (smallLists.isEmpty) {
        if (bigLists.isEmpty) BWLNil
        else if (bigLists.tail.isEmpty) bigLists.head
        else mergeSortTailrec(bigLists, BWLNil)
      } else if (smallLists.tail.isEmpty) {
        if (bigLists.isEmpty) smallLists.head
        else mergeSortTailrec(smallLists.head :: bigLists, BWLNil)
      } else {
        val first  = smallLists.head
        val second = smallLists.tail.head
        val merged = merge(first, second, BWLNil)
        mergeSortTailrec(smallLists.tail.tail, merged :: bigLists)
      }

    val start = Instant.now()
    val value = mergeSortTailrec((left ++ right).map(_ :: BWLNil), BWLNil)
    val end   = Instant.now()
    println("mergeSorted: " + Duration.between(start, end))
    value
  }

}
