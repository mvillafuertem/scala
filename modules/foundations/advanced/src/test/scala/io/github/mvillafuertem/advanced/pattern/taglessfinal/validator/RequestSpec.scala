package io.github.mvillafuertem.advanced.pattern.taglessfinal.validator

import io.github.mvillafuertem.advanced.pattern.taglessfinal.validator.RequestMapper.RequestMapperF
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class RequestSpec extends AnyFlatSpecLike with Matchers {

  implicit val interpreterRequestMapper: RequestMapperF[({ type F[A] = Option[A] })#F] = new RequestMapper {
    type M[A] = Option[A]

    override def mapTo(fa: M[String])(f: String => Int): M[Int] = fa.map(f)

    override def validate(v: M[String]): M[String] =
      v.filter(_.contains("h"))

    override def build(v: String): M[String] = Option(v)
  }

  behavior of "RequestSpec"

  it should "Request as future" in {

    // g i v e n
    val build = Request[Option].build("Hola")

    // w h e n
    val actual: Option[String] = build

    // t h e n
    actual.map(_ shouldBe "Hola")

  }

}
