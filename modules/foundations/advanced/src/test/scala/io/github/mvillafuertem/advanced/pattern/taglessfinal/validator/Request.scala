package io.github.mvillafuertem.advanced.pattern.taglessfinal.validator

trait Request {

  type M[_]

  def build(v: String): M[String]

}

object Request {

  type RequestF[F[_]] = Request {type M[A] = F[A]}

  def apply[F[_] : RequestF]: RequestF[F] = implicitly[RequestF[F]]


  trait ScalaToRequestBridge[A] {
    def apply[F[_]](implicit L: RequestF[F]): F[A]
  }

  def buildType(t: String): ScalaToRequestBridge[String] = new ScalaToRequestBridge[String] {
    override def apply[F[_]](implicit L: RequestF[F]): F[String] = L.build(t)
  }

}
