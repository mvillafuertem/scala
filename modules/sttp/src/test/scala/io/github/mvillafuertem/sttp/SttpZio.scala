package io.github.mvillafuertem.sttp

import io.circe.generic.extras.auto._
import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe.{ asJson, _ }
import zio.test.Assertion.equalTo
import zio.test._

object SttpZio extends DefaultRunnableSpec {

  // @see https://requestbin.com/r/enwf1kwvx6vnf
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class RequestSnakeCase(successSnakeCase: Boolean)

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri                    = "https://enwf1kwvx6vnf.x.pipedream.net/"
  private val requestGET             = basicRequest.get(uri"$uri").response(asJson[Response])
  private val requestPOST            = basicRequest.post(uri"$uri").response(asJson[Response]).body[RequestSnakeCase](RequestSnakeCase(true))
  private val requestPUT             = basicRequest.put(uri"$uri").response(asJson[Response]).body[Request](Request(true))
  private val requestDELETE          = basicRequest.delete(uri"$uri").response(asJson[Response])
  private val equalToResponseSuccess = equalTo[Response, Response](Response(true))

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      testM(s"${requestGET.toCurl}")(
        assertM(
          send(requestGET)
            .map(_.body)
            .absolve
            .provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPOST.toCurl}")(
        assertM(
          send(requestPOST)
            .map(_.body)
            .absolve
            .provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPUT.toCurl}")(
        assertM(
          send(requestPUT)
            .map(_.body)
            .absolve
            .provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      ),
      testM(s"${requestDELETE.toCurl}")(
        assertM(
          send(requestDELETE)
            .map(_.body)
            .absolve
            .provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPOST.toCurl} ++ ${requestGET.toCurl}")(
        assertM(
          (
            for {
              responsePOST <- send(requestPOST).map(_.body)
              responseGET  <- send(requestGET).map(_.body).absolve if responsePOST.isRight
            } yield responseGET
          ).provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPUT.toCurl} ++ ${requestDELETE.toCurl}")(
        assertM(
          (
            for {
              responsePOST <- send(requestPUT).map(_.body)
              responseGET  <- send(requestDELETE).map(_.body).absolve if responsePOST.isRight
            } yield responseGET
          ).provideCustomLayer(AsyncHttpClientZioBackend.layer())
        )(equalToResponseSuccess)
      )
    ) @@ zio.test.TestAspect.flaky

}
