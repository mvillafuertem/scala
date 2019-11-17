package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

trait LanguageBasic[F[_]] {
  def number(v: Int): F[Int]
  def increment(a: F[Int]): F[Int]
  def add(a: F[Int], b: F[Int]): F[Int]
  def text(v: String): F[String]
  def toUpper(a: F[String]): F[String]
  def concat(a: F[String], b: F[String]): F[String]
  def toString(v: F[Int]): F[String]
}

object LanguageBasic {

  trait ScalaToLanguageBasicBridge[A] {
    def apply[F[_]](implicit L: LanguageBasic[F]): F[A]
  }

  def buildNumber(number: Int) = new ScalaToLanguageBasicBridge[Int] {
    override def apply[F[_]](implicit L: LanguageBasic[F]): F[Int] = L.number(number)
  }
  def buildIncrementNumber(number: Int) = new ScalaToLanguageBasicBridge[Int] {
    override def apply[F[_]](implicit L: LanguageBasic[F]): F[Int] = L.increment(L.number(number))
  }
  def buildIncrementExpression(expression: ScalaToLanguageBasicBridge[Int]) = new ScalaToLanguageBasicBridge[Int] {
    override def apply[F[_]](implicit L: LanguageBasic[F]): F[Int] = L.increment(expression.apply)
  }

  // builds an expression like: println(s"$text ${a + (b + 1)}")
  def buildComplexExpression(text: String, a: Int, b: Int) = new ScalaToLanguageBasicBridge[String] {
    override def apply[F[_]](implicit L: LanguageBasic[F]): F[String] = {
      val addition = L.add(L.number(a), L.increment(L.number(b)))
      L.concat(L.text(text), L.toString(addition))
    }
  }

}
