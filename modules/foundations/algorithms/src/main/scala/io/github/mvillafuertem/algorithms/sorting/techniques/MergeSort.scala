package io.github.mvillafuertem.algorithms.sorting.techniques

import io.github.mvillafuertem.algorithms.sorting.techniques.BackwardsList.BWLNil
import io.github.mvillafuertem.algorithms.sorting.techniques.BackwardsList.BWLCons

import java.time.{Duration, Instant}
import scala.annotation.tailrec

trait MergeSort[F[_]] {

  def mergeSorted[A](left: F[A], right: F[A])(implicit ord: Ordering[A]): F[A]
}

object MergeSort {

  def apply[F[_]: MergeSort]: MergeSort[F] = implicitly[MergeSort[F]]

  def mergeSortedIntLists(left: List[Int], right: List[Int]): List[Int] = {

    def loop(list: List[Int]): List[Int] =
      list match {
        case Nil         => Nil
        case head :: Nil => List(head)
        case _           =>
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
    val end   = Instant.now()
    println("mergeSortedIntLists: " + Duration.between(start, end))
    value
  }

  def mergeSortedLists[A](left: List[A], right: List[A])(implicit lessThan: (A, A) => Boolean): List[A] = {

    def loop(list: List[A])(implicit lessThan: (A, A) => Boolean): List[A] =
      list match {
        case Nil         => Nil
        case head :: Nil => List(head)
        case _           =>
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
  import scala.annotation.tailrec


  def mergeSortedBottomUpF[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ordering: Ordering[A]): BackwardsList[A] = {
    // Función auxiliar para mezclar dos sublistas ordenadas
    def merge(listA: BackwardsList[A], listB: BackwardsList[A]): BackwardsList[A] = {
      @tailrec
      def mergeRec(tempListA: BackwardsList[A], tempListB: BackwardsList[A], mergedList: BackwardsList[A]): BackwardsList[A] =
        (tempListA, tempListB) match {
          case (BWLNil, _) =>
            mergedList.foldLeft(tempListB)((acc, elem) => elem :: acc)
          case (_, BWLNil) =>
            mergedList.foldLeft(tempListA)((acc, elem) => elem :: acc)
          case (BWLCons(hA, tA), BWLCons(hB, tB)) =>
            if (ordering.lteq(hA, hB)) {
              mergeRec(tA, tempListB, hA :: mergedList)
            } else {
              mergeRec(tempListA, tB, hB :: mergedList)
            }
        }

      mergeRec(listA, listB, BWLNil)
    }

    // Función para dividir la lista en sublistas de tamaño 1
    def splitList(list: BackwardsList[A]): BackwardsList[BackwardsList[A]] = {
      @tailrec
      def splitRec(tempList: BackwardsList[A], result: BackwardsList[BackwardsList[A]]): BackwardsList[BackwardsList[A]] =
        tempList match {
          case BWLNil =>
            result
          case BWLCons(h, t) =>
            splitRec(t, (h :: BWLNil) :: result)
        }

      splitRec(list, BWLNil).reverse
    }

    // Función para mezclar todas las sublistas ordenadas
    @tailrec
    def mergeAll(sortedLists: BackwardsList[BackwardsList[A]]): BackwardsList[A] =
      sortedLists match {
        case BWLNil =>
          BWLNil
        case BWLCons(h, BWLNil) =>
          h
        case BWLCons(h, t) =>
          val mergedList = merge(h, t.head)
          mergeAll(mergedList :: t.tail)
      }

    val splitLeft = splitList(left)
    val splitRight = splitList(right)
    val mergedList = mergeAll(splitLeft ++ splitRight)
    mergedList
  }


  // Implementación de Merge Sort Bottom-Up para List
  implicit val listMergeSort: MergeSort[List] = new MergeSort[List] {
    override def mergeSorted[A](left: List[A], right: List[A])(implicit ord: Ordering[A]): List[A] = {

      @tailrec
      def merge(left: List[A], right: List[A], acc: List[A]): List[A] = (left, right) match {
        case (Nil, Nil) => acc
        case (Nil, rHead :: rTail) => merge(Nil, rTail, rHead :: acc)
        case (lHead :: lTail, Nil) => merge(lTail, Nil, lHead :: acc)
        case (lHead :: lTail, rHead :: rTail) =>
          if (ord.lt(lHead, rHead)) merge(lTail, right, lHead :: acc)
          else merge(left, rTail, rHead :: acc)
      }

      @tailrec
      def mergePairs(list: List[List[A]], acc: List[List[A]]): List[List[A]] = list match {
        case Nil => acc
        case head :: Nil => head :: acc
        case l1 :: l2 :: tail => mergePairs(tail, merge(l1, l2, Nil) :: acc)
      }

      mergePairs(left :: right :: Nil, Nil).head.reverse
    }
  }


  // Implementación de Merge Sort Bottom-Up para Vector
  implicit val vectorMergeSort: MergeSort[Vector] = new MergeSort[Vector] {
    override def mergeSorted[A](left: Vector[A], right: Vector[A])(implicit ord: Ordering[A]): Vector[A] = {
      @tailrec
      def merge(l: Vector[A], r: Vector[A], acc: Vector[A]): Vector[A] = (l, r) match {
        case (lVec, rVec) if lVec.isEmpty     => acc ++ rVec
        case (lVec, rVec) if rVec.isEmpty     => acc ++ lVec
        case (lHead +: lTail, rHead +: rTail) =>
          if (ord.lt(lHead, rHead)) merge(lTail, r, acc :+ lHead)
          else merge(l, rTail, acc :+ rHead)
      }

      @tailrec
      def mergePairs(list: Vector[Vector[A]], acc: Vector[Vector[A]]): Vector[Vector[A]] = list match {
        case lVec +: rVec +: tail => mergePairs(tail, merge(lVec, rVec, Vector.empty) +: acc)
        case _                    => acc
      }

      mergePairs(left +: right +: Vector.empty, Vector.empty).head
    }
  }


}
