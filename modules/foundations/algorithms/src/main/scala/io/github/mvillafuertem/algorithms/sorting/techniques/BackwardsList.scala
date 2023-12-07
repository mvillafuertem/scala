package io.github.mvillafuertem.algorithms.sorting.techniques

import io.github.mvillafuertem.algorithms.sorting.techniques.BackwardsList.{ mergeSortedBottomUpF, BWLCons, BWLNil }

import scala.annotation.tailrec
import scala.util.Random

sealed trait BackwardsList[+A] {
  def head: A

  def tail: BackwardsList[A]

  def isEmpty: Boolean

  def reverse: BackwardsList[A]

  def ::[S >: A](elem: S): BackwardsList[S]

  def ++[S >: A](anotherList: BackwardsList[S]): BackwardsList[S]

  def flatMap[S](f: A => BackwardsList[S]): BackwardsList[S]

  def map[S](f: A => S): BackwardsList[S]

  def foldLeft[S](seed: S)(f: (S, A) => S): S

  def foldRight[S](seed: S)(f: (A, S) => S): S

}

object BackwardsList {

  // Constructor auxiliar para BackwardsList
  def apply[A](items: A*): BackwardsList[A] = {
    @scala.annotation.tailrec
    def loop(items: Seq[A], acc: BackwardsList[A]): BackwardsList[A] =
      items match {
        case Nil => acc
        case head +: tail => loop(tail, BWLCons(head, acc))
      }

    loop(items.reverse, BWLNil)
  }

  case object BWLNil extends BackwardsList[Nothing] {
    override def isEmpty: Boolean = true

    override def head: Nothing = throw new NoSuchElementException

    override def tail: BackwardsList[Nothing] = throw new NoSuchElementException

    override def reverse: BackwardsList[Nothing] = BWLNil

    override def ++[S >: Nothing](anotherList: BackwardsList[S]): BackwardsList[S] = anotherList

    override def flatMap[S](f: Nothing => BackwardsList[S]): BackwardsList[S] = BWLNil

    override def map[S](f: Nothing => S): BackwardsList[S] = BWLNil

    def ::[S >: Nothing](elem: S): BackwardsList[S] = BWLCons(elem, this)

    override def foldLeft[S](seed: S)(f: (S, Nothing) => S): S = seed

    override def foldRight[S](seed: S)(f: (Nothing, S) => S): S = seed

  }

  case class BWLCons[A](head: A, tail: BackwardsList[A]) extends BackwardsList[A] {
    override def isEmpty: Boolean = false

    override def reverse: BackwardsList[A] =
      foldLeft(empty[A])((acc, elem) => BWLCons(elem, acc))

    override def foldLeft[S](seed: S)(f: (S, A) => S): S = {
      @tailrec
      def foldRec(list: BackwardsList[A], accumulator: S): S = list match {
        case BWLNil              => accumulator
        case BWLCons(head, tail) => foldRec(tail, f(accumulator, head))
      }

      foldRec(this, seed)
    }

    override def foldRight[S](seed: S)(f: (A, S) => S): S = {
      @tailrec
      def foldRec(list: BackwardsList[A], accumulator: S): S = list match {
        case BWLNil              => accumulator
        case BWLCons(head, tail) => foldRec(tail, f(head, accumulator))
      }

      foldRec(this, seed)
    }

    override def ++[S >: A](anotherList: BackwardsList[S]): BackwardsList[S] =
      foldRight(anotherList)(_ :: _)

    override def flatMap[S](f: A => BackwardsList[S]): BackwardsList[S] =
      foldLeft(empty[S])((acc, elem) => f(elem) ++ acc)

    override def map[S](f: A => S): BackwardsList[S] =
      foldRight(empty[S])((elem, acc) => f(elem) :: acc)

    def ::[S >: A](elem: S): BackwardsList[S] = BWLCons(elem, this)

  }

  def empty[A]: BackwardsList[A] = BWLNil

  def mergeSortedTopBottom[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ordering: Ordering[A]): BackwardsList[A] = {

    @tailrec
    def merge(listA: BackwardsList[A], listB: BackwardsList[A], accumulator: BackwardsList[A]): BackwardsList[A] =
      (listA, listB) match {
        case (BWLNil, _)                                                           => accumulator.reverse ++ listB
        case (_, BWLNil)                                                           => accumulator.reverse ++ listA
        case (BWLCons(headA, _), BWLCons(headB, _)) if ordering.lteq(headA, headB) => merge(listA.tail, listB, headA :: accumulator)
        case (BWLCons(_, _), BWLCons(headB, tailB))                                => merge(listA, tailB, headB :: accumulator)
      }

    @tailrec
    def mergeSortTailrec(smallLists: BackwardsList[BackwardsList[A]], bigLists: BackwardsList[BackwardsList[A]]): BackwardsList[A] =
      (smallLists, bigLists) match {
        case (BWLNil, BWLNil)                => BWLNil
        case (BWLNil, BWLCons(head, BWLNil)) => head
        case (BWLNil, _)                     => mergeSortTailrec(bigLists, BWLNil)
        case (BWLCons(head, BWLNil), BWLNil) => head
        case (BWLCons(head, BWLNil), _)      => mergeSortTailrec(head :: bigLists, BWLNil)
        case (BWLCons(_, _), _)              =>
          val merged = merge(smallLists.head, smallLists.tail.head, BWLNil)
          mergeSortTailrec(smallLists.tail.tail, merged :: bigLists)
      }

    mergeSortTailrec((left ++ right).map(_ :: BWLNil), BWLNil)
  }

  def mergeSortedBottomUpI[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ordering: Ordering[A]): BackwardsList[A] = {
    // Función auxiliar para mezclar dos sublistas ordenadas
    def merge(listA: BackwardsList[A], listB: BackwardsList[A]): BackwardsList[A] = {
      var mergedList: BackwardsList[A] = BWLNil
      var tempListA                    = listA
      var tempListB                    = listB

      while (!tempListA.isEmpty && !tempListB.isEmpty)
        if (ordering.lteq(tempListA.head, tempListB.head)) {
          mergedList = tempListA.head :: mergedList
          tempListA = tempListA.tail
        } else {
          mergedList = tempListB.head :: mergedList
          tempListB = tempListB.tail
        }

      while (!tempListA.isEmpty) {
        mergedList = tempListA.head :: mergedList
        tempListA = tempListA.tail
      }

      while (!tempListB.isEmpty) {
        mergedList = tempListB.head :: mergedList
        tempListB = tempListB.tail
      }

      mergedList.reverse
    }

    // Función para dividir la lista en sublistas de tamaño 1
    def splitList(list: BackwardsList[A]): BackwardsList[BackwardsList[A]] = {
      var tempList                                = list
      var result: BackwardsList[BackwardsList[A]] = BWLNil

      while (!tempList.isEmpty) {
        result = (tempList.head :: BWLNil) :: result
        tempList = tempList.tail
      }

      result
    }

    // Función para mezclar todas las sublistas ordenadas
    def mergeAll(sortedLists: BackwardsList[BackwardsList[A]]): BackwardsList[A] = {
      var tempList = sortedLists

      while (tempList.tail != BWLNil) {
        val mergedList = merge(tempList.head, tempList.tail.head)
        tempList = mergedList :: tempList.tail.tail
      }

      tempList.head
    }

    val mergedList = mergeAll(splitList(left ++ right))
    mergedList
  }

  def mergeSortedBottomUpF[A](left: BackwardsList[A], right: BackwardsList[A])(implicit ordering: Ordering[A]): BackwardsList[A] = {
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

object MergeSortTests extends App {

  def printList[A](fa: BackwardsList[A]): Unit = {
    def printListRec[A](fa: BackwardsList[A]): Unit = fa match {
      case BWLNil                            => ()
      case BackwardsList.BWLCons(head, tail) =>
        print(s"$head ")
        printListRec(tail)
    }
    printListRec(fa)
    println("")
    println("_______________")
  }

  def isSorted[A](list: BackwardsList[A])(implicit ordering: Ordering[A]): Boolean = {
    @tailrec
    def checkSorted(tempList: BackwardsList[A], prevElem: Option[A]): Boolean =
      tempList match {
        case BWLNil        => true
        case BWLCons(h, t) =>
          prevElem match {
            case Some(prev) if ordering.gt(prev, h) => false
            case _                                  => checkSorted(t, Some(h))
          }
      }

    checkSorted(list, None)
  }

  // Prueba con lista vacía
  val emptyList: BackwardsList[Int] = BackwardsList.BWLNil
  assert(isSorted(mergeSortedBottomUpF(emptyList, emptyList)))
  printList(mergeSortedBottomUpF(emptyList, emptyList))

  // Prueba con una lista ya ordenada
  val sortedList: BackwardsList[Int] = BackwardsList.BWLCons(1, BackwardsList.BWLCons(2, BackwardsList.BWLCons(3, BackwardsList.BWLCons(4, BWLNil))))
  assert(isSorted(mergeSortedBottomUpF(sortedList, emptyList)))
  assert(isSorted(mergeSortedBottomUpF(emptyList, sortedList)))
  assert(isSorted(mergeSortedBottomUpF(sortedList, sortedList)))
  printList(mergeSortedBottomUpF(sortedList, emptyList))
  printList(mergeSortedBottomUpF(emptyList, sortedList))
  printList(mergeSortedBottomUpF(sortedList, sortedList))

  // Prueba con una lista en orden inverso
  val reversedList: BackwardsList[Int] = BackwardsList.BWLCons(4, BackwardsList.BWLCons(3, BackwardsList.BWLCons(2, BackwardsList.BWLCons(1, BWLNil))))
  assert(isSorted(mergeSortedBottomUpF(reversedList, emptyList)))
  assert(isSorted(mergeSortedBottomUpF(emptyList, reversedList)))
  assert(isSorted(mergeSortedBottomUpF(reversedList, reversedList)))
  printList(mergeSortedBottomUpF(reversedList, emptyList))
  printList(mergeSortedBottomUpF(emptyList, reversedList))
  printList(mergeSortedBottomUpF(reversedList, reversedList))

  // Prueba con lista de elementos repetidos
  val repeatedList: BackwardsList[Int] = BackwardsList.BWLCons(2, BackwardsList.BWLCons(2, BackwardsList.BWLCons(2, BackwardsList.BWLCons(2, BWLNil))))
  assert(isSorted(mergeSortedBottomUpF(repeatedList, emptyList)))
  assert(isSorted(mergeSortedBottomUpF(emptyList, repeatedList)))
  assert(isSorted(mergeSortedBottomUpF(repeatedList, repeatedList)))
  printList(mergeSortedBottomUpF(repeatedList, emptyList))
  printList(mergeSortedBottomUpF(emptyList, repeatedList))
  printList(mergeSortedBottomUpF(repeatedList, repeatedList))

  // Prueba con lista de elementos aleatorios
  val randomList: BackwardsList[Int] = {
    val random   = new Random()
    val elements = List.fill(100000)(random.nextInt(100000)) // Crear lista de 100 elementos aleatorios entre 0 y 99
    elements.foldLeft(emptyList)((a, b) => a.::(b)) // Convertir la lista en una BackwardsList
  }
  printList(mergeSortedBottomUpF(randomList, emptyList))
  printList(mergeSortedBottomUpF(emptyList, randomList))
  printList(mergeSortedBottomUpF(randomList, randomList))

  println("All tests passed!")
}

object Main extends App {

  val list1: BackwardsList[Int] = BackwardsList.BWLCons(1, BackwardsList.BWLCons(2, BWLNil))
  val list2: BackwardsList[Int] = BackwardsList.BWLCons(3, BackwardsList.BWLCons(4, BackwardsList.BWLCons(5, BWLNil)))

  val concatenatedList: BackwardsList[Int] = list1 ++ (list2)

  // Output: 1 2 3 4 5
  def printList[A](fa: BackwardsList[A]): Unit = fa match {
    case BWLNil                            => ()
    case BackwardsList.BWLCons(head, tail) =>
      print(s"$head ")
      printList(tail)
  }

  printList(concatenatedList) // Output: 1 2 3 4 5

  println("////////////")

  val list: BackwardsList[Int] = BackwardsList.BWLCons(4, BackwardsList.BWLCons(2, BackwardsList.BWLCons(3, BackwardsList.BWLCons(1, BWLNil))))
  val sortedList               = BackwardsList.mergeSortedBottomUpF(list, list)
  printList(sortedList) // Output: 1 1 2 2 3 3 4 4
}
