package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

import io.github.mvillafuertem.advanced.pattern.taglessfinal.basic.LanguageBasicWithTypeMembers.LanguageBasicWithTypeMembersF

object InterpreterLanguageWithTypeMembersBasic {

  type F[A] = A
  val interpreter: LanguageBasicWithTypeMembersF[F] = new LanguageBasicWithTypeMembers {
    override type Monad[A] = A
    override def number(v: Int): Monad[Int]                    = v
    override def increment(a: Monad[Int]): Monad[Int]          = a + 1
    override def add(a: Monad[Int], b: Monad[Int]): Monad[Int] = a + b

    override def text(v: String): Monad[String]                            = v
    override def toUpper(a: Monad[String]): Monad[String]                  = a.toUpperCase
    override def concat(a: Monad[String], b: Monad[String]): Monad[String] = a + " " + b

    override def toString(v: Monad[Int]): Monad[String] = v.toString
  }

}
