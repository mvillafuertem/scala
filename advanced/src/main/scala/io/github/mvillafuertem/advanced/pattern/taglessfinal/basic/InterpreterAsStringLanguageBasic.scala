package io.github.mvillafuertem.advanced.pattern.taglessfinal.basic

object InterpreterAsStringLanguageBasic {

  type F[ScalaValue] = String

  val interpreterAsString: LanguageBasic[F] = new LanguageBasic[F] {
    override def number(v: Int): F[Int] = s"($v)"
    override def increment(a: F[Int]): F[Int] = s"(inc $a)"
    override def add(a: F[Int], b: F[Int]): F[Int] = s"(+ $a $b)"

    override def text(v: String): F[String] = s"[$v]"
    override def toUpper(a: F[String]): F[String] = s"(toUpper $a)"
    override def concat(a: F[String], b: F[String]): F[String] = s"(concat $a $b)"

    override def toString(v: F[Int]): F[String] = s"(toString $v)"
  }

}
