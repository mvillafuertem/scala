package io.github.mvillafuertem.foundations

// https://www.youtube.com/watch?v=LmBTiFYa-V4
trait Semigroup[T] {
  // debe cumplir ley Asociatividad
  // a + b == b + a
  // (a + b) + c == a + (b + c)
  def combine(a: T, b: T): T
}

object Semigroup {

  // another syntax
  // def apply[T](using instance: Semigroup[T]): Semigroup[T] = instance
  def apply[T: Semigroup]: Semigroup[T] = implicitly[Semigroup[T]]

  /**
    * Scala 2 Syntax
    * 
    * implicit val intSemigroup: Semigroup[Int] = new Semigroup[Int]
    *   def combine(a: Int, b: Int): Int = a + b
    */
// This is commented because a Monoid is a Semigroup
//
//  given intSemigroup: Semigroup[Int] with
//    def combine(a: Int, b: Int): Int = a + b
//
//  given stringSemigroup: Semigroup[String] with
//    def combine(a: String, b: String): String = a + b
  import Monoid.given
  
  /**
    * Scala 2 Syntax
    *
    * implicit class SemigroupSyntax[T: Semigroup](a: T) {
    *   def |+|(b: T): T = Semigroup[T].combine(a, b)
    */
  extension [T: Semigroup](a: T) 
    def |+|(b: T): T = Semigroup[T].combine(a, b)
  
  def test(): Unit = {
    val naturalIntSemigroup = Semigroup[Int](using intMonoid) // explicitly
    val naturalStringSemigroup = Semigroup[String]// implicitly
    
    val meaningOfLife = naturalIntSemigroup.combine(2, 42)
    val favLanguage = naturalStringSemigroup.combine("Sca", "la")
    
    println(meaningOfLife)
    println(favLanguage)
    
    assert(meaningOfLife == 44)
    assert(favLanguage == "Scala")
    
    def reduceInts(list: List[Int]): Int = list.reduce(_ +_)
    def reduceStrings(list: List[String]): String = list.reduce(_ +_)
    
    // another syntax
    // def reduceThings[T](list: List[T])(using semigroup: Semigroup[T]): T = list.reduce(semigroup.combine)
    def reduceThings[T: Semigroup](list: List[T]): T = list.reduce(implicitly[Semigroup[T]].combine)

    val reduceListOfInt = reduceThings(List(1, 2, 3))
    val reduceListOfString = reduceThings(List("i", "love", "scala"))
    
    println(reduceListOfInt)
    println(reduceListOfString)
    
    assert(reduceListOfInt == 6)
    assert(reduceListOfString == "ilovescala")
    
    val semigroupSyntax = 2 |+| 3 |+| 4
    println(semigroupSyntax)
    assert(semigroupSyntax == 9)
    
    def reduceCompact[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)

    val reduceCompactListOfInt = reduceCompact(List(1, 2, 3))
    println(reduceListOfInt)
    assert(reduceListOfInt == 6)

  }
  
}
