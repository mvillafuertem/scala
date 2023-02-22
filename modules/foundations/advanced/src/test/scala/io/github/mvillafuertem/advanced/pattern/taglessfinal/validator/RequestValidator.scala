package io.github.mvillafuertem.advanced.pattern.taglessfinal.validator

trait RequestValidator extends Request {
  def validate(v: M[String]): M[String]

}

object RequestValidator {

  type ValidatorRequestF[F[_]] = RequestValidator {type M[A] = F[A]}


  trait ScalaToValidatorRequestBridge[A] {
    def apply[F[_]](implicit L: ValidatorRequestF[F]): F[A]
  }

  def validate(v: Request.ScalaToRequestBridge[String]): ScalaToValidatorRequestBridge[String] = new ScalaToValidatorRequestBridge[String] {
    override def apply[F[_]](implicit L: ValidatorRequestF[F]): F[String] = L.validate(v[F])
  }


}
