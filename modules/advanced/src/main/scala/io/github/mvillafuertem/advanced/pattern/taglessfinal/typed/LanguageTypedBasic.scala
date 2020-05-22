package io.github.mvillafuertem.advanced.pattern.taglessfinal.typed

trait LanguageTypedBasic[F[_]] {
  def build[T](v: T): F[T]
}

object LanguageTypedBasic {

  trait ScalaToLanguageBasicBridge[A] {
    def apply[F[_]](implicit L: LanguageTypedBasic[F]): F[A]
  }

  def buildType[T](t: T): ScalaToLanguageBasicBridge[T] =
    new ScalaToLanguageBasicBridge[T] {
      override def apply[F[_]](implicit L: LanguageTypedBasic[F]): F[T] = L.build[T](t)

    }
}
