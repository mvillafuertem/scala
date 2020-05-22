package io.github.mvillafuertem.advanced.pattern.taglessfinal.typed

object InterpreterTypedBasic {

  type F[ScalaValue] = ScalaValue

  val interpreterExtended: LanguageTypedBasic[F] = new LanguageTypedBasic[F] {
    override def build[T](v: T): F[T] = v
  }

}
