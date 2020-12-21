package io.github.mvillafuertem.akka.http

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpHeader, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.github.mvillafuertem.akka.http.AkkaHTTPWithJWT.{ auth, securedContent, AccessTokenHeaderName, LoginRequest }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class AkkaHTTPWithJWTSpec extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  private val routes: Route = auth ~ securedContent

  behavior of s"${getClass.getSimpleName}"

  it should "return 403 Unauthorized upon GET / " in {
    Get("/hello").addHeader(RawHeader(AccessTokenHeaderName, "asdfads")) ~> routes ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }

  it should "return 403 Unauthorized when credentials are incorrect" in {
    Post("/auth/login", LoginRequest("admin", "something")) ~> routes ~> check {
      header(AccessTokenHeaderName) shouldBe Some(_: HttpHeader)
      status shouldBe StatusCodes.Unauthorized
    }
  }

  it should "return JWT token upon POST /" in {
    Post("/auth/login", LoginRequest("admin", "admin")) ~> routes ~> check {
      header(AccessTokenHeaderName) shouldBe Some(_: HttpHeader)
      status shouldBe StatusCodes.OK
    }
  }

  it should "access secured content after providing a correct JWT upon GET /" in {
    Post("/auth/login", LoginRequest("admin", "admin")) ~> routes ~> check {
      header(AccessTokenHeaderName).map { accessTokenHeader =>
        Get("/hello").addHeader(RawHeader(AccessTokenHeaderName, accessTokenHeader.value())) ~> routes ~> check {
          status shouldBe StatusCodes.OK
        }
      }
    }

  }

}
