//package io.github.mvillafuertem.algorithms.sorting.techniques
//
//import io.github.mvillafuertem.algorithms.sorting.techniques.BackwardsList.{ BWLCons, BWLNil, BackwardsList }
//import io.github.mvillafuertem.algorithms.sorting.techniques.MergeSort._
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpecLike
//
//final class MergeSortTest extends AnyWordSpecLike with Matchers {
//
//  "mergeSortedIntLists" should {
//
//    "return an empty List when leftList and rightList are empty" in {
//      mergeSortedIntLists(List.empty[Int], List.empty[Int]) shouldBe List.empty[Int]
//    }
//
//    "return the same List when leftList contains only one element and rightList is empty" in {
//      mergeSortedIntLists(List(1), List.empty[Int]) shouldBe List(1)
//    }
//
//    "return the same List when rightList contains only one element and leftList is empty" in {
//      mergeSortedIntLists(List.empty[Int], List(1)) shouldBe List(1)
//    }
//
//    "return the ordered List when leftList contains elements and rightList is empty" in {
//      mergeSortedIntLists(List(13, 8, 5, 3, 2, 1), List.empty[Int]) shouldBe List(1, 2, 3, 5, 8, 13)
//    }
//
//    "return the ordered List when rightList contains elements and leftList is empty" in {
//      mergeSortedIntLists(List.empty[Int], List(13, 8, 5, 3, 2, 1)) shouldBe List(1, 2, 3, 5, 8, 13)
//    }
//
//    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
//      mergeSortedIntLists(List(1, 2, 3, 5, 8, 13), List(55, 21, 34)) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
//    }
//
//    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
//      mergeSortedIntLists(List(55, 21, 34), List(1, 2, 3, 5, 8, 13)) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
//    }
//  }
//
//  "mergeSortedLists[Int]" should {
//
//    "return an empty List when leftList and rightList are empty" in {
//      mergeSortedLists(List.empty[Int], List.empty[Int])(_ < _) shouldBe List.empty[Int]
//    }
//
//    "return the same List when leftList contains only one element and rightList is empty" in {
//      mergeSortedLists(List(1), List.empty[Int])(_ < _) shouldBe List(1)
//    }
//
//    "return the same List when rightList contains only one element and leftList is empty" in {
//      mergeSortedLists(List.empty[Int], List(1))(_ < _) shouldBe List(1)
//    }
//
//    "return the ordered List when leftList contains elements and rightList is empty" in {
//      mergeSortedLists(List(13, 8, 5, 3, 2, 1), List.empty[Int])(_ < _) shouldBe List(1, 2, 3, 5, 8, 13)
//    }
//
//    "return the ordered List when rightList contains elements and leftList is empty" in {
//      mergeSortedLists(List.empty[Int], List(13, 8, 5, 3, 2, 1))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13)
//    }
//
//    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
//      mergeSortedLists(List(1, 2, 3, 5, 8, 13), List(55, 21, 34))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
//    }
//
//    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
//      mergeSortedLists(List(55, 21, 34), List(1, 2, 3, 5, 8, 13))(_ < _) shouldBe List(1, 2, 3, 5, 8, 13, 21, 34, 55)
//    }
//  }
//
//  "mergeSortedLists[String]" should {
//    "return an empty List when leftList and rightList are empty" in {
//      mergeSortedLists(List.empty[String], List.empty[String])(_ < _) shouldBe List.empty[String]
//    }
//
//    "return the same List when leftList contains only one element and rightList is empty" in {
//      mergeSortedLists(List("a"), List.empty[String])(_ < _) shouldBe List("a")
//    }
//
//    "return the same List when rightList contains only one element and leftList is empty" in {
//      mergeSortedLists(List.empty[String], List("a"))(_ < _) shouldBe List("a")
//    }
//
//    "return the ordered List when leftList contains elements and rightList is empty" in {
//      mergeSortedLists(List("f", "e", "d", "c", "b", "a"), List.empty[String])(_ < _) shouldBe List("a", "b", "c", "d", "e", "f")
//    }
//
//    "return the ordered List when rightList contains elements and leftList is empty" in {
//      mergeSortedLists(List.empty[String], List("f", "e", "d", "c", "b", "a"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f")
//    }
//
//    "return the ordered List when leftList contains ordered elements and rightList is unordered" in {
//      mergeSortedLists(List("a", "b", "c", "d", "e", "f"), List("i", "h", "g"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f", "g", "h", "i")
//    }
//
//    "return the ordered List when rightList contains ordered elements and leftList is unordered" in {
//      mergeSortedLists(List("i", "h", "g"), List("a", "b", "c", "d", "e", "f"))(_ < _) shouldBe List("a", "b", "c", "d", "e", "f", "g", "h", "i")
//    }
//  }
//
//  "Merge Sort for BackwardsList" should {
//
//    "return an empty BackwardsList when sorting an empty BackwardsList" in {
//      val emptyList: BackwardsList[Int] = BWLNil
//      BackwardsList.mergeSorted(emptyList, emptyList) should be(emptyList)
//    }
//
//    "return ordered BackwardsList" in {
//      val right: BackwardsList[Int] = BWLCons(2, BWLNil)
//      BackwardsList.mergeSorted(BWLCons(2, BWLCons(1, BWLNil)), BWLNil) should be(BWLCons(1, BWLCons(2, BWLNil)))
//    }
//
//    "correctly sort a BackwardsList of integers in ascending order" in {
//      val unsortedList: BackwardsList[Int]   = BWLCons(5, BWLCons(2, BWLCons(7, BWLCons(1, BWLCons(3, BWLCons(6, BWLCons(4, BWLNil)))))))
//      val expectedSorted: BackwardsList[Int] = BWLCons(1, BWLCons(2, BWLCons(3, BWLCons(4, BWLCons(5, BWLCons(6, BWLCons(7, BWLNil)))))))
//      BackwardsList.mergeSorted(unsortedList, BWLNil) should be(expectedSorted)
//    }
//
////    "correctly sort a BackwardsList of strings in ascending alphabetical order" ignore {
////      val unsortedList: BackwardsList[String] = BWLCons("banana", BWLCons("apple", BWLCons("orange", BWLCons("grape", BWLCons("cherry", BWLNil)))))
////      val expectedSorted: BackwardsList[String] = BWLCons("apple", BWLCons("banana", BWLCons("cherry", BWLCons("grape", BWLCons("orange", BWLNil)))))
////      MergeSort[BackwardsList](BackwardsList.backwardsListMergeSortBottomUp).mergeSorted(unsortedList, unsortedList) should be(expectedSorted)
////    }
////
////    "handle a large BackwardsList of integers" ignore {
////      val largeList: BackwardsList[Int] = BackwardsList((1 to 1000000): _*)
////      MergeSort[BackwardsList](BackwardsList.backwardsListMergeSortBottomUp).mergeSorted(largeList, largeList) should be(largeList)
////    }
////  }
//
////  "Merge Sort for Vector" ignore {
////
////    "return an empty Vector when sorting an empty Vector" in {
////      val emptyVector: Vector[Int] = Vector.empty[Int]
////      MergeSort[Vector].mergeSorted(emptyVector, emptyVector) should be(emptyVector)
////    }
////
////    "return the same Vector when sorting a single-element Vector" in {
////      val singleElementVector: Vector[Int] = Vector(42)
////      MergeSort[Vector].mergeSorted(singleElementVector, singleElementVector) should be(singleElementVector)
////    }
////
////    "correctly sort a Vector of integers in ascending order" in {
////      val unsortedVector: Vector[Int] = Vector(5, 2, 7, 1, 3, 6, 4)
////      val expectedSorted: Vector[Int] = Vector(1, 2, 3, 4, 5, 6, 7)
////      MergeSort[Vector].mergeSorted(unsortedVector, unsortedVector) should be(expectedSorted)
////    }
////
////    "correctly sort a Vector of strings in ascending alphabetical order" in {
////      val unsortedVector: Vector[String] = Vector("banana", "apple", "orange", "grape", "cherry")
////      val expectedSorted: Vector[String] = Vector("apple", "banana", "cherry", "grape", "orange")
////      MergeSort[Vector].mergeSorted(unsortedVector, unsortedVector) should be(expectedSorted)
////    }
////
////    "handle a large Vector of integers" in {
////      val largeVector: Vector[Int] = (1 to 1000000).toVector
////      MergeSort[Vector].mergeSorted(largeVector, largeVector) should be(largeVector.sorted)
////    }
////  }
////
////  "Merge Sort for List" ignore {
////
////    "return an empty List when sorting an empty List" in {
////      val emptyList: List[Int] = List.empty[Int]
////      MergeSort[List].mergeSorted(emptyList, emptyList) should be(emptyList)
////    }
////
////    "return the same List when sorting a single-element List" in {
////      val singleElementList: List[Int] = List(42)
////      MergeSort[List].mergeSorted(singleElementList, singleElementList) should be(singleElementList)
////    }
////
////    "correctly sort a List of integers in ascending order" in {
////      val unsortedList: List[Int] = List(5, 2, 7, 1, 3, 6, 4)
////      val expectedSorted: List[Int] = List(1, 2, 3, 4, 5, 6, 7)
////      MergeSort[List].mergeSorted(unsortedList, unsortedList) should be(expectedSorted)
////    }
////
////    "correctly sort a List of strings in ascending alphabetical order" in {
////      val unsortedList: List[String] = List("banana", "apple", "orange", "grape", "cherry")
////      val expectedSorted: List[String] = List("apple", "banana", "cherry", "grape", "orange")
////      MergeSort[List].mergeSorted(unsortedList, unsortedList) should be(expectedSorted)
////    }
////
////    "handle a large List of integers" in {
////      val largeList: List[Int] = (1 to 1000000).toList
////      MergeSort[List].mergeSorted(largeList, largeList) should be(largeList.sorted)
////    }
////
////  }
//
//  }
//}
