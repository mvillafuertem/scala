package io.github.mvillafuertem.advanced.implicits


// @see Scala Implicits are Everywhere https://arxiv.org/pdf/1908.07883.pdf
// Simplicitly: Foundations and Applications of Implicit FunctionTypes https://www.youtube.com/watch?v=9Wp_riP8LQw
// https://medium.com/@alexander.zaidel/scala-implicits-in-dotty-5b8905af63f0
object ImplicitsForAdHocPolymorphism extends App {

  def aggregate(l: List[Int]): Int =
    l match {
      case Nil          => 0
      case head :: tail => head + aggregate(tail)
    }

  def aggregateGeneric[T](l: List[T])(zero: T, combine: (T, T) => T): T =
    l match {
      case Nil          => zero
      case head :: tail => combine(aggregateGeneric(tail)(zero, combine): T, head: T)
    }

  aggregate(List(1, 2, 3, 4))
  aggregateGeneric[Int](List(1, 2, 3, 4))(0, _ + _)
  aggregateGeneric[String](List("1", "2", "3", "4"))("0", _ + _)

  /**
   * Esta clase Aggregatable son los llamados monoids, es decir no se llama agregatable,
   * Que ganas usando type clases?,
   * es mucho mas limpio, compara como los métodos se leen mejor
   * y mejoramos el testing gracias a leyes de las type class.
   * Tambien te olvidas de la herenciay los type bounds [T >: A]
   */
  trait Aggregatable[T] {
    val zero: T
    def combine(t1: T, t2: T): T
  }

  object Aggregatable {

    object IntSumIsAgg extends Aggregatable[Int] {
      override val zero: Int = 0

      override def combine(t1: Int, t2: Int): Int = t1 + t2
    }

  }

  def aggregateWithTypeClass[T](l: List[T])(A: Aggregatable[T]): T =
    l match {
      case Nil          => A.zero: T
      case head :: tail => A.combine(head, aggregateWithTypeClass(tail)(A)): T
    }

  /**
   *
   */
  def aggregateWithTypeClassAndImplicit[T](l: List[T])(implicit A: Aggregatable[T]): T =
    l match {
      case Nil          => A.zero: T
      case head :: tail => A.combine(head, aggregateWithTypeClassAndImplicit(tail)): T
    }

  /**
   *
   */
  def aggregateWithTypeClassAndImplicitAndImport[T](l: List[T])(implicit A: Aggregatable[T]): T = {
    import A._
    l match {
      case Nil          => zero: T
      case head :: tail => combine(head, aggregateWithTypeClassAndImplicit(tail)): T
    }
  }

  /**
   *
   */
  def aggregateWithTypeClassAndImplicitlyAndContextBound[T: Aggregatable](l: List[T]): T =
    l match {
      case Nil          => implicitly[Aggregatable[T]].zero: T
      case head :: tail => implicitly[Aggregatable[T]].combine(head, aggregateWithTypeClassAndImplicitlyAndContextBound(tail)): T
    }

}
