package io.github.mvillafuertem.sttp

import io.circe.generic.auto._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.ziostreams.AsyncHttpClientZioStreamsBackend
import sttp.client.circe.{ asJson, _ }
import sttp.client.{ SttpBackend, _ }
import zio.Task
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestEnvironment

object SttpZio extends DefaultRunnableSpec {

  implicit val backend: Task[SttpBackend[Task, Nothing, WebSocketHandler]] = AsyncHttpClientZioStreamsBackend()

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri           = "https://enbom40wuq5zg.x.pipedream.net/1"
  private val requestGET    = basicRequest.get(uri"$uri").response(asJson[Response])
  private val requestPOST   = basicRequest.post(uri"$uri").response(asJson[Response]).body[Request](Request(true))
  private val requestPUT    = basicRequest.put(uri"$uri").response(asJson[Response]).body[Request](Request(true))
  private val requestDELETE = basicRequest.delete(uri"$uri").response(asJson[Response])

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] = suite(getClass.getSimpleName)(
    testM(s"${requestGET.toCurl}")(
      assertM(
        backend
          .flatMap(implicit backend => requestGET.send())
          .map(_.body)
      )(equalTo(Right(Response(true))))
    ),
    testM(s"${requestPOST.toCurl}")(
      assertM(
        backend
          .flatMap(implicit backend => requestPOST.send())
          .map(_.body)
      )(equalTo(Right(Response(true))))
    ),
    testM(s"${requestPUT.toCurl}")(
      assertM(
        backend
          .flatMap(implicit backend => requestPUT.send())
          .map(_.body)
      )(equalTo(Right(Response(true))))
    ),
    testM(s"${requestDELETE.toCurl}")(
      assertM(
        backend
          .flatMap(implicit backend => requestDELETE.send())
          .map(_.body)
      )(equalTo(Right(Response(true))))
    ),
    testM(s"${{ requestPOST.toCurl }} ++ ${requestGET.toCurl}")(
      assertM(
        backend
          .flatMap(implicit backend =>
            for {
              responsePOST <- requestPOST.send().map(_.body)
              responseGET  <- requestGET.send().map(_.body) if responsePOST.isRight
            } yield responseGET
          )
      )(equalTo(Right(Response(true))))
    )
  )

}
