package io.github.mvillafuertem.foundations

trait Monoid[T] extends Semigroup[T] {
  // debe cumplir esta ley, tambien se llama elemento neutro
  // empty + x == x for all x in the type T
  def empty: T
}

object Monoid {

  given intMonoid: Monoid[Int] with {
    def empty: Int                   = 0
    def combine(a: Int, b: Int): Int = a + b
  }

  given stringMonoid: Monoid[String] with {
    def empty: String                         = ""
    def combine(a: String, b: String): String = a + b
  }

  def test(): Unit = ()

}
