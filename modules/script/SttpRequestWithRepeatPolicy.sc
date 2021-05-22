#!/usr/bin/env amm

import $ivy.`dev.zio::zio-test:1.0.8`
import $ivy.`dev.zio::zio:1.0.8`
import $ivy.`com.softwaremill.sttp.client::akka-http-backend:2.2.9`
import $ivy.`com.softwaremill.sttp.client::async-http-client-backend-zio:2.2.9`
import $ivy.`com.softwaremill.sttp.client::circe:2.2.9`
import $ivy.`com.softwaremill.sttp.client::core:2.2.9`
import $ivy.`io.circe::circe-generic:0.13.0`
import $ivy.`io.circe::circe-generic-extras:0.13.0`

import zio.console.{ putStr, putStrLn, Console }
import io.circe.parser._
import zio._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.Live
import io.circe.generic.extras.auto._
import io.circe.generic.extras.{ Configuration, ConfiguredJsonCodec }
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe.{ asJson, _ }
import sttp.client.{ SttpBackend, _ }
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestEnvironment }
import zio.{ Chunk, RIO, Schedule, Task }

// amm `pwd`/app/modules/script/SttpRequestWithRepeatPolicy.sc impl
@main
def impl(): Unit = SttpRequestWithRepeatPolicy.main(Array())

// amm `pwd`/app/modules/script/SttpRequestWithRepeatPolicy.sc spec
@main
def spec(): Unit = SttpRequestWithRepeatPolicySpec.main(Array())

object SttpRequestWithRepeatPolicy extends zio.App {

  // @see https://requestbin.com/r/ene80m1n53nb
  implicit val customConfig: Configuration                        = Configuration.default
  val backend: Task[SttpBackend[Task, Nothing, WebSocketHandler]] = AsyncHttpClientZioBackend()

  case class Response(success: Boolean)
  private val uri = "https://ene80m1n53nb.x.pipedream.net/"
  val requestGET  = basicRequest.get(uri"$uri").response(asJson[Response])

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    AsyncHttpClientZioBackend().flatMap { implicit backend =>
      requestGET
        .send()
        .map(_.body)
        .absolve
        .repeat(
          (Schedule.spaced(2.second) >>>
            Schedule.recurWhile[Long](_ < 5))
            .tapOutput[Console](n => putStr(n.toString + " ").exitCode) *>
            Schedule
              .collectAll[Response]
              .tapInput[Console, Response](response => putStrLn(response.toString).exitCode)
        )
        .ensuring(backend.close().ignore)
    }.exitCode
}

object SttpRequestWithRepeatPolicySpec extends DefaultRunnableSpec {

  // @see https://requestbin.com/r/ene80m1n53nb
  implicit val customConfig: Configuration                        = Configuration.default
  val backend: Task[SttpBackend[Task, Nothing, WebSocketHandler]] = AsyncHttpClientZioBackend()

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri        = "https://ene80m1n53nb.x.pipedream.net/"
  private val requestGET = basicRequest.get(uri"$uri").response(asJson[Response])

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      testM(s"${requestGET.toCurl} ++ repeat policy")(
        assertM(
          backend.flatMap { implicit backend =>
            for {
              fiber <- requestGET
                         .send()
                         .map(_.body)
                         .absolve
                         .repeat(
                           (Schedule.spaced(2.second) >>>
                             Schedule.recurWhile[Long](_ < 5))
                             .tapOutput[Console](n => putStr(n.toString + " ").exitCode) *>
                             Schedule
                               .collectAll[Response]
                               .tapInput[Console, Response](response => putStrLn(response.toString).exitCode)
                         )
                         .catchAll(a =>
                           zio.console.putStr(a.getMessage) >>>
                             RIO.effect(Chunk(Response(false)))
                         )
                         .fork
              _     <- TestClock.adjust(20.seconds)
              _     <- fiber.join.ensuring(backend.close().ignore)
            } yield ()
          }
        )(equalTo(()))
      )
    )

}
