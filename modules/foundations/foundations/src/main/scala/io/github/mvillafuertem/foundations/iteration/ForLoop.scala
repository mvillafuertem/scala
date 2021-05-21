package io.github.mvillafuertem.foundations.iteration

object ForLoop {

  def sum(numbers: List[Int]): Int =
    foldLeft(numbers, 0)(_ + _)
//    var total = 0
//    for (number <- numbers) total += number
//    total

  // a. Implement `size` using a mutable state and a for loop
  // such as size(List(2,5,1,8)) == 4
  // and     size(Nil) == 0
  def size[A](items: List[A]): Int =
    foldLeft(items, 0)((state, _) => state + 1)
//    var size = 0
//    for (_ <- items) size += 1
//    size

  // b. Implement `min` using a mutable state and a for loop
  // such as min(List(2,5,1,8)) == Some(1)
  // and     min(Nil) == None
  // Note: Option is an enumeration with two values:
  // * Some when there is a value and
  // * None when there is no value (a bit like null)
  def min(numbers: List[Int]): Option[Int] =
    foldLeft(numbers, Option.empty[Int]) {
      case (Some(currentMin), number) => Some(number min currentMin)
      case (None, number)             => Some(number)
    }
//    var state = Option.empty[Int]
//    for (number <- numbers)
//      state match {
//        case None             => state = Some(number)
//        case Some(currentMin) => state = Some(number min currentMin)
//      }
//    state

  // c. Implement `wordCount` using a mutable state and a for loop.
  // `wordCount` compute how many times each word appears in a `List`
  // such as wordCount(List("Hi", "Hello", "Hi")) == Map("Hi" -> 2, "Hello" -> 1)
  // and     wordCount(Nil) == Map.empty
  // Note: You can lookup an element in a `Map` with the method `get`
  // and you can upsert a value using `updated`
  def wordCount(words: List[String]): Map[String, Int] =
    foldLeft(words, Map.empty[String, Int]) { (frequency, word) =>
      frequency.updatedWith(word) {
        case Some(value) => Some(value + 1)
        case None        => Some(1)
      }
    }
//    var state = Map.empty[String, Int]
//    for (word <- words)
//      state = state.updatedWith(word) {
//        case Some(value) => Some(value + 1)
//        case None        => Some(1)
//      }
  // 1. Solución
  // val frequency = state.getOrElse(word, 0)
  // state = state.updated(word, frequency + 1)

  // 2. Solución
  //     state.get(word) match {
  //       case Some(value) => state = state.updated(word, value + 1)
  //       case None => state = state.updated(word, 1)
  //     }
  // state

  // d. `sum`, `size`, `min` and `wordCount` are quite similar.
  // Could you write a higher-order function that captures this pattern?
  // How would you call it?
  def foldLeft[From, To](items: List[From], defaultValue: To)(combine: (To, From) => To): To = {
    var state = defaultValue
    for (item <- items) state = combine(state, item)
    state
  }

  // e. Refactor `sum`, `size`, `min` and `wordCount` using the higher-order
  // function you defined above.

  //////////////////////////////////////////////
  // Bonus question (not covered by the video)
  //////////////////////////////////////////////

  // f. `foldLeft` can be used to implement most of the List API.
  // Do you want to give it a try? For example, can you implement
  // `map`, `reverse` and `lastOption` in terms of `foldLeft`
  def map[From, To](elements: List[From])(update: From => To): List[To] =
    reverse(foldLeft(elements, List.empty[To])((state, element) => update(element) :: state))

  // reverse(List(3,8,1)) == List(1,8,3)
  // reverse(Nil) == Nil
  def reverse[A](elements: List[A]): List[A] =
    foldLeft(elements, List.empty[A])((state, element) => element :: state)

  // lastOption(List(3,8,1)) == Some(1)
  // lastOption(Nil) == None
  def lastOption[A](elements: List[A]): Option[A] =
    foldLeft(elements, Option.empty[A])((_, element) => Some(element))

  // g. Can you generalise `min` so that it applies to more types like `Long`, `String`, ...?
  // Note: You may want to use the class Ordering from the standard library
  def generalMin[A](elements: List[A])(ord: Ordering[A]): Option[A] =
    foldLeft(elements, Option.empty[A]) {
      case (None, element)        => Some(element)
      case (Some(state), element) => Some(ord.min(state, element))
    }

  // Instance of Ordering are generally passed implicitly using the typeclass pattern (not covered).
  def generalMin2[A](elements: List[A])(implicit ord: Ordering[A]): Option[A] =
    generalMin(elements)(ord)
}
