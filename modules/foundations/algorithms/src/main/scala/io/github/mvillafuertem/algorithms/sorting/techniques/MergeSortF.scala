package io.github.mvillafuertem.algorithms.sorting.techniques

import io.github.mvillafuertem.algorithms.sorting.techniques.BackwardsList.{BWLCons, BWLNil}

import scala.annotation.tailrec
import scala.collection.immutable.{Vector, VectorBuilder}

trait MergeSortF[F[_]] {
  def mergeSorted[A](left: F[A], right: F[A])(implicit ord: Ordering[A]): F[A]
}

object MergeSortF {

  def apply[F[_]: MergeSortF]: MergeSortF[F] = implicitly[MergeSortF[F]]

  implicit val backwardslistMergeSortF: MergeSortF[BackwardsList] = new MergeSortF[BackwardsList] {
    override def mergeSorted[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ord: Ordering[A]): BackwardsList[A] = {
      // Función auxiliar para mezclar dos sublistas ordenadas
      def merge(listA: BackwardsList[A], listB: BackwardsList[A]): BackwardsList[A] = {
        @tailrec
        def mergeRec(tempListA: BackwardsList[A], tempListB: BackwardsList[A], mergedList: BackwardsList[A]): BackwardsList[A] =
          (tempListA, tempListB) match {
            case (BWLNil, _)                        =>
              mergedList.foldLeft(tempListB)((acc, elem) => elem :: acc)
            case (_, BWLNil)                        =>
              mergedList.foldLeft(tempListA)((acc, elem) => elem :: acc)
            case (BWLCons(hA, tA), BWLCons(hB, tB)) =>
              if (ord.lteq(hA, hB)) {
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
            case BWLNil        =>
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
          case BWLNil             =>
            BWLNil
          case BWLCons(h, BWLNil) =>
            h
          case BWLCons(h, t)      =>
            val mergedList = merge(h, t.head)
            mergeAll(mergedList :: t.tail)
        }

      val splitLeft  = splitList(left)
      val splitRight = splitList(right)
      val mergedList = mergeAll(splitLeft ++ splitRight)
      mergedList
    }
  }


  implicit val listMergeSortF: MergeSortF[List] = new MergeSortF[List] {
    def mergeSorted[A](left: List[A], right: List[A])(implicit ordering: Ordering[A]): List[A] = {
      // Función auxiliar para mezclar dos listas ordenadas
      @tailrec
      def merge(left: List[A], right: List[A], acc: List[A]): List[A] =
        (left, right) match {
          case (Nil, _)                                       => acc.reverse ::: right
          case (_, Nil)                                       => acc.reverse ::: left
          case (leftHead :: leftTail, rightHead :: rightTail) =>
            if (ordering.lteq(leftHead, rightHead)) {
              merge(leftTail, right, leftHead :: acc)
            } else {
              merge(left, rightTail, rightHead :: acc)
            }
        }

      // Función para dividir la lista en dos mitades
      @tailrec
      def split(list: List[A], left: List[A], right: List[A]): (List[A], List[A]) =
        list match {
          case Nil     => (left.reverse, right)
          case x :: xs => split(xs, right, x :: left)
        }

      // Función para realizar merge sort recursivamente
      def mergeSort(list: List[A]): List[A] =
        list match {
          case Nil      => Nil
          case x :: Nil => list
          case _        =>
            val (left, right) = split(list, Nil, Nil)
            merge(mergeSort(left), mergeSort(right), Nil)
        }

      mergeSort(left ++ right)
    }
  }

//  implicit val vectorMergeSortFO: MergeSortF[Vector] = new MergeSortF[Vector] {
//    override def mergeSorted[A](left: Vector[A], right: Vector[A])(implicit ord: Ordering[A]): Vector[A] = {
//
//      def merge(l: Vector[A], r: Vector[A], acc: Vector[A]): Vector[A] = (l, r) match {
//        case (lVec, rVec) if lVec.isEmpty     => acc ++ rVec
//        case (lVec, rVec) if rVec.isEmpty     => acc ++ lVec
//        case (lHead +: lTail, rHead +: rTail) =>
//          if (ord.lt(lHead, rHead)) merge(lTail, r, acc :+ lHead)
//          else merge(l, rTail, acc :+ rHead)
//      }
//
//      def mergePairs(list: Vector[Vector[A]], acc: Vector[Vector[A]]): Vector[Vector[A]] = list match {
//        case lVec +: rVec +: tail => mergePairs(tail, merge(lVec, rVec, Vector.empty) +: acc)
//        case singleVec +: tail    => mergePairs(tail, singleVec +: acc)
//        case _                    => acc
//      }
//
//      var mergedPairs = mergePairs(left.map(Vector(_)) ++ right.map(Vector(_)), Vector.empty)
//
//      while (mergedPairs.size > 1) {
//        mergedPairs = mergePairs(mergedPairs, Vector.empty)
//      }
//
//      mergedPairs.headOption.getOrElse(Vector.empty)
//    }
//  }

  implicit val vectorMergeSortF: MergeSortF[Vector] = new MergeSortF[Vector] {
    override def mergeSorted[A](left: Vector[A], right: Vector[A])(implicit ord: Ordering[A]): Vector[A] = {

      def merge(l: Vector[A], r: Vector[A], acc: Vector[A]): Vector[A] = (l, r) match {
        case (lVec, rVec) if lVec.isEmpty => acc ++ rVec
        case (lVec, rVec) if rVec.isEmpty => acc ++ lVec
        case (lHead +: lTail, rHead +: rTail) =>
          if (ord.lt(lHead, rHead)) merge(lTail, r, acc :+ lHead)
          else merge(l, rTail, acc :+ rHead)
      }

      val mergedPairs = left.map(Vector(_)) ++ right.map(Vector(_))

      @scala.annotation.tailrec
      def mergeAll(list: Vector[Vector[A]]): Vector[A] = {
        if (list.size > 1) {
          val merged = list.grouped(2).map {
            case lVec +: rVec +: _ => merge(lVec, rVec, Vector.empty)
            case singleVec +: _ => singleVec
            case _ => Vector.empty
          }.toVector
          mergeAll(merged)
        } else {
          list.headOption.getOrElse(Vector.empty)
        }
      }

      mergeAll(mergedPairs)
    }
  }

}
