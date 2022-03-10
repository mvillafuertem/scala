package io.github.mvillafuertem.sttp

import io.circe
import io.circe.generic.extras.auto._
import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import org.asynchttpclient.DefaultAsyncHttpClient
import sttp.client3
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe.{ asJson, _ }
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{ endpoint, stringBody, DecodeResult }
import zio.Task
import zio.test.Assertion.equalTo
import zio.test._

object SttpZio extends DefaultRunnableSpec {

  // @see https://requestbin.com/r/enwf1kwvx6vnf
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class RequestSnakeCase(successSnakeCase: Boolean)

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri           = "https://enwf1kwvx6vnf.x.pipedream.net/"
  private val requestGET    = basicRequest.get(uri"$uri").response(asJson[Response])
  private val requestPOST   = basicRequest.post(uri"$uri").response(asJson[Response]).body[RequestSnakeCase](RequestSnakeCase(true))
  private val requestPUT    = basicRequest.put(uri"$uri").response(asJson[Response]).body[Request](Request(true))
  private val requestDELETE = basicRequest.delete(uri"$uri").response(asJson[Response])
  private val baseEndpoint  = endpoint.out(jsonBody[Response]).errorOut(stringBody)

  private val requestEndpointGET: RequestT[Identity, Either[ResponseException[String, circe.Error], Response], Any] =
    SttpClientInterpreter()
      .toRequest(baseEndpoint.get, Some(uri"$uri"))
      .apply()
      .response(asJson[Response])

  private val requestEndpointPOST: client3.Request[Response, Any] =
    SttpClientInterpreter()
      .toRequestThrowErrors(baseEndpoint.in(jsonBody[Request]).post, Some(uri"$uri"))
      .apply(Request(true))

  private val requestEndpointPUT: Task[DecodeResult[Either[String, Response]]] =
    SttpClientInterpreter()
      .toClient(baseEndpoint.in(jsonBody[Request]).put, Some(uri"$uri"), AsyncHttpClientZioBackend.usingClient(runner.runtime, new DefaultAsyncHttpClient()))
      .apply(Request(true))

  private val requestEndpointDELETE: Task[Response] =
    SttpClientInterpreter()
      .toClientThrowErrors(baseEndpoint.delete, Some(uri"$uri"), AsyncHttpClientZioBackend.usingClient(runner.runtime, new DefaultAsyncHttpClient()))
      .apply()

  private val equalToResponseSuccess = equalTo[Response, Response](Response(true))

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      test(s"${requestGET.toCurl}")(
        assertM(
          send(requestGET)
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      test(s"${requestPOST.toCurl}")(
        assertM(
          send(requestPOST)
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      test(s"${requestPUT.toCurl}")(
        assertM(
          send(requestPUT)
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      test(s"${requestDELETE.toCurl}")(
        assertM(
          send(requestDELETE)
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      test(s"${requestPOST.toCurl} ++ ${requestGET.toCurl}")(
        assertM(
          for {
            responsePOST <- send(requestPOST).map(_.body)
            responseGET  <- send(requestGET).map(_.body).absolve if responsePOST.isRight
          } yield responseGET
        )(equalToResponseSuccess)
      ),
      test(s"${requestPUT.toCurl} ++ ${requestDELETE.toCurl}")(
        assertM(
          for {
            responsePOST <- send(requestPUT).map(_.body)
            responseGET  <- send(requestDELETE).map(_.body).absolve if responsePOST.isRight
          } yield responseGET
        )(equalToResponseSuccess)
      ),
      test(s"${requestEndpointGET.toCurl}")(
        assertM(
          send(requestEndpointGET)
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      test(s"${requestEndpointPOST.toCurl}")(
        assertM(
          send(requestEndpointPOST)
            .map(_.body)
        )(equalToResponseSuccess)
      ),
      test("requestEndpointPUT")(
        assertM(requestEndpointPUT.map {
          case _: DecodeResult.Failure => Left(new RuntimeException("DecodeResult.Failure"))
          case DecodeResult.Value(v)   =>
            v match {
              case Left(value)  => Left(new RuntimeException(value))
              case Right(value) => Right(value)
            }
        }.absolve)(equalToResponseSuccess)
      ),
      test("requestEndpointDELETE")(
        assertM(requestEndpointDELETE)(equalToResponseSuccess)
      )
    ).provideCustomLayer(
      AsyncHttpClientZioBackend.layer().mapError(TestFailure.fail)
    ) @@ zio.test.TestAspect.flaky

}
