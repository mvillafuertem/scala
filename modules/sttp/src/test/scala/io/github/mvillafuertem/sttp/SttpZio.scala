package io.github.mvillafuertem.sttp

import io.circe.generic.extras.auto._
import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.ziostreams.AsyncHttpClientZioStreamsBackend
import sttp.client.circe.{ asJson, _ }
import sttp.client.{ SttpBackend, _ }
import zio.Task
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestEnvironment

object SttpZio extends DefaultRunnableSpec {

  // @see https://requestbin.com/r/enbom40wuq5zg/1bOSAYwoE7KoRARt3fMJQCHdOF7
  implicit val customConfig: Configuration                                 = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class RequestSnakeCase(successSnakeCase: Boolean)
  implicit val backend: Task[SttpBackend[Task, Nothing, WebSocketHandler]] = AsyncHttpClientZioStreamsBackend()

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri                    = "https://enbom40wuq5zg.x.pipedream.net/"
  private val requestGET             = basicRequest.get(uri"$uri").response(asJson[Response])
  private val requestPOST            = basicRequest.post(uri"$uri").response(asJson[Response]).body[RequestSnakeCase](RequestSnakeCase(true))
  private val requestPUT             = basicRequest.put(uri"$uri").response(asJson[Response]).body[Request](Request(true))
  private val requestDELETE          = basicRequest.delete(uri"$uri").response(asJson[Response])
  private val equalToResponseSuccess = equalTo(Response(true))

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      testM(s"${requestGET.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend => requestGET.send())
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPOST.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend => requestPOST.send())
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPUT.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend => requestPUT.send())
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      testM(s"${requestDELETE.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend => requestDELETE.send())
            .map(_.body)
            .absolve
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPOST.toCurl} ++ ${requestGET.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend =>
              for {
                responsePOST <- requestPOST.send().map(_.body)
                responseGET  <- requestGET.send().map(_.body).absolve if responsePOST.isRight
              } yield responseGET
            )
        )(equalToResponseSuccess)
      ),
      testM(s"${requestPUT.toCurl} ++ ${requestDELETE.toCurl}")(
        assertM(
          backend
            .flatMap(implicit backend =>
              for {
                responsePOST <- requestPUT.send().map(_.body)
                responseGET  <- requestDELETE.send().map(_.body).absolve if responsePOST.isRight
              } yield responseGET
            )
        )(equalToResponseSuccess)
      )
    ) @@ TestAspect.ignore // TODO waiting sttp updates

}
