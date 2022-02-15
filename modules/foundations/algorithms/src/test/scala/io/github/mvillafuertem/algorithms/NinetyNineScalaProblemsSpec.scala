package io.github.mvillafuertem.algorithms

import io.github.mvillafuertem.algorithms.NinetyNineScalaProblemsSpec._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.annotation.tailrec

class NinetyNineScalaProblemsSpec extends AnyFlatSpecLike with Matchers {

  /**
   * http://aperiodic.net/phil/scala/s-99/
   */
  "all" should "pass" in {
    p01(List(1, 1, 2, 3, 5, 8)) shouldBe Some(8)
    p02(List(1, 1, 2, 3, 5, 8)) shouldBe Some(5)
    p03(2, List(1, 1, 2, 3, 5, 8)) shouldBe Some(2)
    p04(List(1, 1, 2, 3, 5, 8)) shouldBe 6
    p05(List(1, 1, 2, 3, 5, 8)) shouldBe List(8, 5, 3, 2, 1, 1)
    p06(List(1, 2, 3, 2, 1)) shouldBe true
    p07(List(List(1, 1), 2, List(3, List(5, 8)))) shouldBe List(1, 1, 2, 3, 5, 8)
    p08(List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")) shouldBe List("a", "b", "c", "a", "d", "e")
    p09(List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")) shouldBe List(
      List("a", "a", "a", "a"),
      List("b"),
      List("c", "c"),
      List("a", "a"),
      List("d"),
      List("e", "e", "e", "e")
    )
    p10(List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")) shouldBe List((4, "a"), (1, "b"), (2, "c"), (2, "a"), (1, "d"), (4, "e"))
    // p11(List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")) shouldBe List((4,"a"), "b", (2,"c"), (2,"a"), "d", (4,"e"))
    p12(List((4, "a"), (1, "b"), (2, "c"), (2, "a"), (1, "d"), (4, "e"))) shouldBe List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")
    p13(List("a", "a", "a", "a", "b", "c", "c", "a", "a", "d", "e", "e", "e", "e")) shouldBe List((4, "a"), (1, "b"), (2, "c"), (2, "a"), (1, "d"), (4, "e"))
    p14(List("a", "b", "c", "c", "d")) shouldBe List("a", "a", "b", "b", "c", "c", "c", "c", "d", "d")
    p15(3, List("a", "b", "c", "c", "d")) shouldBe List("a", "a", "a", "b", "b", "b", "c", "c", "c", "c", "c", "c", "d", "d", "d")
    p16(3, List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")) shouldBe List("a", "b", "d", "e", "g", "h", "j", "k")
    p17(3, List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")) shouldBe (List("a", "b", "c"), List("d", "e", "f", "g", "h", "i", "j", "k"))
    p18(3, 7, List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")) shouldBe List("d", "e", "f", "g")
    p19(3, List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k")) shouldBe List("d", "e", "f", "g", "h", "i", "j", "k", "a", "b", "c")
    p20(1, List("a", "b", "c", "d")) shouldBe (List("a", "c", "d"), "b")
    p21("new", 1, List("a", "b", "c", "d")) shouldBe List("a", "new", "b", "c", "d")
    p22(4, 9) shouldBe List(4, 5, 6, 7, 8, 9)
    p23(3, List("a", "b", "c", "d", "e", "f", "g", "h")) should have size 3
    p23(3, List("a", "b", "c", "d", "e", "f", "g", "h")) should contain atLeastOneOf ("a", "b", "c", "d", "e", "f", "g", "h")
    p24(6, 50) should have size 6
    p25(List("a", "b", "c", "d", "e", "f", "g", "h")) should have size 8
    p31(2) shouldBe true
  }

}

object NinetyNineScalaProblemsSpec {

  @tailrec
  def p01[T](list: List[T]): Option[T] = list match {
    case Nil           => None
    case ::(head, Nil) => Some(head)
    case ::(_, next)   => p01(next)
  }

  @tailrec
  def p02[T](list: List[T]): Option[T] = list match {
    case Nil                => None
    case ::(head, _ :: Nil) => Some(head)
    case ::(_, next)        => p02(next)
  }

  @tailrec
  def p03[T](i: Int, list: List[T]): Option[T] = list match {
    case Nil                   => None
    case ::(head, _) if i == 0 => Some(head)
    case ::(_, next)           => p03(i - 1, next)
  }

  def p04[T](list: List[T]): Int = {
    @tailrec
    def _p04(x: List[T], acc: Int): Int = x match {
      case Nil         => acc
      case ::(_, next) => _p04(next, acc + 1)
    }

    _p04(list, 0)
  }

  def p05[T](list: List[T]): List[T] = {
    @tailrec
    def _p05(x: List[T], y: List[T]): List[T] = x match {
      case Nil            => y
      case ::(head, next) => _p05(next, head :: y)
    }

    _p05(list, Nil)
  }

  def p06[T](list: List[T]): Boolean = {
    @tailrec
    def _p06(x: List[T], y: List[T]): List[T] = x match {
      case Nil            => y
      case ::(head, next) => _p06(next, head :: y)
    }

    list == _p06(list, Nil)
  }

  def p07[T](list: List[T]): List[T] = list.flatMap {
    case x: List[T] => p07(x)
    case y          => List(y)
  }

  def p08[T](list: List[T]): List[T] = list match {
    case Nil                               => Nil
    case head :: Nil                       => List(head)
    case head :: next if head == next.head => p08(next)
    case head :: next                      => head :: p08(next)
  }

  def p09[T](list: List[T]): List[List[T]] = {
    @tailrec
    def _p09(x: List[T], y: List[List[T]]): List[List[T]] = x match {
      case Nil                                              => y
      case head :: next if y.isEmpty || y.last.head != head => _p09(next, y ::: List(List(head)))
      case head :: next                                     => _p09(next, y.init ::: List(y.last ::: List(head)))
    }

    _p09(list, Nil)
  }

  def p10[T](list: List[T]): List[(Int, T)] =
    p09(list)
      .map(e => (e.length, e.head))

  //  def p11[T](list: List[T]): List[Any] =
  //    p09(list)
  //      .map(e => e.length match {
  //        case _ <= 1 => e.head
  //        case _ => (e.length, e.head)
  //      })

  def p12[T](list: List[(Int, T)]): List[T] = {
    @tailrec
    def _p12(y: (Int, T), x: List[T]): List[T] = y match {
      case (0, _)    => x
      case (n, head) => _p12((n - 1, head), x ::: List(head))
    }

    list.flatMap(_p12(_, Nil))
  }

  def p13[T](list: List[T]): List[(Int, T)] = {
    @tailrec
    def _p13(x: List[T], y: List[(Int, T)]): List[(Int, T)] = x match {
      case Nil => y
      case _   =>
        val (span, next) = x.span(_ == x.head)
        _p13(next, y ::: List((span.length, span.head)))
    }

    _p13(list, Nil)
  }

  //  def p14[T](list: List[T]): List[T] = {
  //    @tailrec
  //    def _p14(n: Int, e: T, list: List[T]): List[T] = n match {
  //      case 0 => list
  //      case _ => _p14(n - 1, e, list ::: List(e))
  //    }
  //
  //    list.flatMap(_p14(2, _, Nil))
  //  }

  def p14[T](list: List[T]): List[T] = {
    @tailrec
    def _p14(x: List[T], n: Int, list: List[T]): List[T] = x match {
      case Nil                   => list
      case ::(_, next) if n == 0 => _p14(next, 2, list)
      case ::(head, _)           => _p14(x, n - 1, list ::: List(head))
    }

    _p14(list, 2, Nil)
  }

  def p15[T](n: Int, list: List[T]): List[T] = {
    @tailrec
    def _p15(x: List[T], i: Int, list: List[T]): List[T] = x match {
      case Nil                   => list
      case ::(_, next) if i == 0 => _p15(next, n, list)
      case ::(head, _)           => _p15(x, i - 1, list ::: List(head))
    }

    _p15(list, n, Nil)
  }

  def p16[T](n: Int, list: List[T]): List[T] = {
    @tailrec
    def _p16(x: List[T], i: Int, list: List[T]): List[T] = x match {
      case Nil                   => list
      case ::(_, next) if i == 1 => _p16(next, n, list)
      case ::(head, next)        => _p16(next, i - 1, list ::: List(head))
    }

    _p16(list, n, Nil)
  }

  def p17[T](n: Int, list: List[T]): (List[T], List[T]) = {
    @tailrec
    def _p17(x: List[T], i: Int, lists: (List[T], List[T])): (List[T], List[T]) = x match {
      case Nil            => lists
      case next if i == 0 => _p17(Nil, n, (lists._1, next))
      case ::(head, next) => _p17(next, i - 1, (lists._1 ::: List(head), Nil))
    }

    _p17(list, n, (Nil, Nil))
  }

  def p18[T](i: Int, j: Int, list: List[T]): List[T] = {
    @tailrec
    def _p18(x: List[T], n: Int, list: List[T]): List[T] = x match {
      case Nil                               => list
      case ::(head, next) if n >= i && n < j => _p18(next, n + 1, list ::: List(head))
      case ::(_, next)                       => _p18(next, n + 1, list)
    }

    _p18(list, 0, Nil)
  }

  def p19[T](n: Int, list: List[T]): List[T] = {
    @tailrec
    def _p19(x: List[T], i: Int, list: List[T]): List[T] = x match {
      case Nil            => list
      case next if i == n => _p19(Nil, n, next ::: list)
      case ::(head, next) => _p19(next, i + 1, list ::: List(head))
    }

    _p19(list, 0, Nil)
  }

  def p20[T](n: Int, list: List[T]): (List[T], T) = {
    @tailrec
    def _p20(x: List[T], i: Int, list: List[T], e: => T): (List[T], T) = x match {
      case Nil                      => (list, e)
      case ::(head, next) if i == n => _p20(Nil, n, list ::: next, head)
      case ::(head, next)           => _p20(next, i + 1, head :: list, e)
    }

    _p20(list, 0, Nil, throw new NoSuchElementException)
  }

  def p21[T](e: T, n: Int, list: List[T]): List[T] = {
    @tailrec
    def _p21(x: List[T], i: Int, list: List[T]): List[T] = x match {
      case Nil            => list
      case next if i == n => _p21(Nil, n, list ::: List(e) ::: next)
      case ::(head, next) => _p21(next, i + 1, head :: list)
    }

    _p21(list, 0, Nil)
  }

  def p22(i: Int, j: Int): List[Int] = {
    @tailrec
    def _p22(start: Int, end: Int, list: List[Int]): List[Int] =
      if (start <= end) {
        _p22(start + 1, end, list ::: List(start))
      } else {
        list
      }

    _p22(i, j, Nil)
  }

  def p23[A](n: Int, list: List[A]): List[A] = {
    def _p23(n: Int, ls: List[A], r: util.Random): List[A] =
      if (n <= 0) Nil
      else {
        val (rest, e) = p20(r.nextInt(ls.length), ls)
        e :: _p23(n - 1, rest, r)
      }

    _p23(n, list, new util.Random)
  }

  def p24[A](n: Int, list: List[A]): List[A] = {
    def _p23(n: Int, ls: List[A], r: util.Random): List[A] =
      if (n <= 0) Nil
      else {
        val (rest, e) = p20(r.nextInt(ls.length), ls)
        e :: _p23(n - 1, rest, r)
      }

    _p23(n, list, new util.Random)
  }

  def p24(n: Int, max: Int): List[Int] =
    p23(n, List.range(1, max + 1))

  def p25[T](list: List[T]): List[T] =
    p23(list.length, list)

  def p31[T](n: Int): Boolean =
    (n >= 2) && _p31(n, n - 1)

  @tailrec
  private def _p31[T](n: Int, i: Int): Boolean = n match {
    case _ if i <= 1 => true
    case _ if n % i == 0 => false
    case _ => _p31(n, i - 1)
  }

}
