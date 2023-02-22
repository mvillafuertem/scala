package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

trait LanguageBasicWithTypeMembers {

  type Monad[_]

  def number(v: Int): Monad[Int]

  def increment(a: Monad[Int]): Monad[Int]

  def add(a: Monad[Int], b: Monad[Int]): Monad[Int]

  def text(v: String): Monad[String]

  def toUpper(a: Monad[String]): Monad[String]

  def concat(a: Monad[String], b: Monad[String]): Monad[String]

  def toString(v: Monad[Int]): Monad[String]

}

object LanguageBasicWithTypeMembers {

  type LanguageBasicWithTypeMembersF[F[_]] = LanguageBasicWithTypeMembers { type Monad[A] = F[A] }

  trait ScalaToLanguageBasicBridge[A] {
    def apply[F[_]](implicit L: LanguageBasicWithTypeMembersF[F]): F[A]
  }

  def buildNumber(number: Int): ScalaToLanguageBasicBridge[Int] =
    new ScalaToLanguageBasicBridge[Int] {
      override def apply[F[_]](implicit L: LanguageBasicWithTypeMembersF[F]): F[Int] = L.number(number)
    }

  def buildIncrementNumber(number: Int): ScalaToLanguageBasicBridge[Int] =
    new ScalaToLanguageBasicBridge[Int] {
      override def apply[F[_]](implicit L: LanguageBasicWithTypeMembersF[F]): F[Int] = L.increment(L.number(number))
    }

  def buildIncrementExpression(expression: ScalaToLanguageBasicBridge[Int]): ScalaToLanguageBasicBridge[Int] =
    new ScalaToLanguageBasicBridge[Int] {
      override def apply[F[_]](implicit L: LanguageBasicWithTypeMembersF[F]): F[Int] = L.increment(expression.apply)
    }

  // builds an expression like: println(s"$text ${a + (b + 1)}")
  def buildComplexExpression(text: String, a: Int, b: Int): ScalaToLanguageBasicBridge[String] =
    new ScalaToLanguageBasicBridge[String] {
      override def apply[F[_]](implicit L: LanguageBasicWithTypeMembersF[F]): F[String] = {
        val addition = L.add(L.number(a), L.increment(L.number(b)))
        L.concat(L.text(text), L.toString(addition))
      }
    }

}
