package io.github.mvillafuertem.advanced.pattern.taglessfinal.extended

import io.github.mvillafuertem.advanced.pattern.taglessfinal.basic.LanguageBasic

trait LanguageBasicExtended[F[_]] extends LanguageBasic[F] {
  def multiply(a: F[Int], b: F[Int]): F[Int]
}

object LanguageBasicExtended {

  def apply[F[_]: LanguageBasicExtended]: LanguageBasicExtended[F] = implicitly

  trait ScalaToLanguageBasicExtendedBridge[A] {
    def apply[F[_]](implicit L: LanguageBasicExtended[F]): F[A]
  }

  def multiply(a: Int, b: Int) =
    new ScalaToLanguageBasicExtendedBridge[Int] {
      override def apply[F[_]](implicit L: LanguageBasicExtended[F]): F[Int] =
        L.multiply(L.number(a), L.number(b))
    }

}
