package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

object InterpreterLanguageBasic {

  type F[ScalaValue] = ScalaValue

  val interpreter: LanguageBasic[F] = new LanguageBasic[F] {
    override def number(v: Int): F[Int]            = v
    override def increment(a: F[Int]): F[Int]      = a + 1
    override def add(a: F[Int], b: F[Int]): F[Int] = a + b

    override def text(v: String): F[String]                    = v
    override def toUpper(a: F[String]): F[String]              = a.toUpperCase
    override def concat(a: F[String], b: F[String]): F[String] = a + " " + b

    override def toString(v: F[Int]): F[String] = v.toString
  }

}
