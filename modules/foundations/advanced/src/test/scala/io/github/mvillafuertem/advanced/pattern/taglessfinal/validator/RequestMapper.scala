package io.github.mvillafuertem.advanced.pattern.taglessfinal.validator

trait RequestMapper extends RequestValidator {
  def mapTo(fa: M[String])(f: String => Int): M[Int]
}

object RequestMapper {
  type RequestMapperF[F[_]] = RequestMapper { type M[A] = F[A] }

  def apply[F[_]: RequestMapperF]: RequestMapperF[F] = implicitly[RequestMapperF[F]]

  trait ScalaToRequestMapperBridge[B] {
    def apply[F[_]](implicit L: RequestMapperF[F]): F[B]
  }

  def mapTo(fa: RequestValidator.ScalaToValidatorRequestBridge[String])(f: String => Int): ScalaToRequestMapperBridge[Int] =
    new ScalaToRequestMapperBridge[Int] {
      override def apply[F[_]](implicit L: RequestMapperF[F]): F[Int] =
        L.mapTo(fa.apply[F])(f)
    }

}
