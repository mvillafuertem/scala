#!/usr/bin/env amm

import $ivy.`com.softwaremill.sttp.client3::akka-http-backend:3.3.5`
import $ivy.`com.softwaremill.sttp.client3::async-http-client-backend-zio:3.3.5`
import $ivy.`com.softwaremill.sttp.client3::circe:3.3.5`
import $ivy.`com.softwaremill.sttp.client3::core:3.3.5`
import $ivy.`dev.zio::zio-test:1.0.10`
import $ivy.`dev.zio::zio:1.0.10`
import $ivy.`io.circe::circe-generic-extras:0.14.1`
import $ivy.`io.circe::circe-generic:0.14.0`
import $ivy.`io.circe::circe-parser:0.14.0`
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import sttp.client3._
import sttp.client3.asynchttpclient.zio._
import sttp.client3.circe._
import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{TestClock, TestEnvironment}
import zio.{Chunk, RIO, Schedule, _}

// amm `pwd`/app/modules/script/SttpRequestWithRepeatPolicy.sc impl
@main
def impl(): Unit = SttpRequestWithRepeatPolicy.main(Array())

// amm `pwd`/app/modules/script/SttpRequestWithRepeatPolicy.sc spec
@main
def spec(): Unit = SttpRequestWithRepeatPolicySpec.main(Array())

object SttpRequestWithRepeatPolicy extends zio.App {

  // @see https://requestbin.com/r/ene80m1n53nb
  implicit val customConfig: Configuration = Configuration.default

  case class Response(success: Boolean)
  private val uri = "https://ene80m1n53nb.x.pipedream.net/"
  val requestGET  = basicRequest.get(uri"$uri").response(asJson[Response])

  val program: ZIO[Console with SttpClient with Clock, Throwable, Chunk[Response]] = send(requestGET)
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

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    program
      .provideCustomLayer[Throwable, SttpClient](AsyncHttpClientZioBackend.layer())
      .exitCode
}

object SttpRequestWithRepeatPolicySpec extends DefaultRunnableSpec {

  // @see https://requestbin.com/r/ene80m1n53nb
  implicit val customConfig: Configuration = Configuration.default

  case class Response(success: Boolean)
  case class Request(success: Boolean)
  private val uri        = "https://ene80m1n53nb.x.pipedream.net/"
  private val requestGET = basicRequest.get(uri"$uri").response(asJson[Response])

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      testM(s"${requestGET.toCurl} ++ repeat policy")(
        assertM(
          (for {
            fiber  <- AsyncHttpClientZioBackend().flatMap(backend =>
                        backend
                          .send(requestGET)
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
                            zio.console.putStr(a.toString) >>>
                              RIO.effect(Chunk(Response(false)))
                          )
                          .fork
                      )
            _      <- TestClock.adjust(20.seconds)
            actual <- fiber.join
          } yield actual)
        )(equalTo(Chunk()))
      )
    )
}
