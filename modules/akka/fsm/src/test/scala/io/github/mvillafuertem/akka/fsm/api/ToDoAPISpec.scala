package io.github.mvillafuertem.akka.fsm.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.circe.parser.decode
import io.github.mvillafuertem.akka.fsm.configuration.ToDoConfiguration
import io.github.mvillafuertem.akka.fsm.BuildInfo
import ToDoAPI.{HealthInfo, _}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * @author Miguel Villafuerte
 */
final class ToDoAPISpec extends ToDoConfiguration
  with AnyFlatSpecLike
  with ScalatestRouteTest
  with Matchers {

  behavior of "ToDo API"

  it should "actuator" in {

    // W H E N
    Get("/api/v1.0/health") ~>
      toDoAPI.routes ~>
      // T H E N
      check {
        status shouldBe StatusCodes.OK
        header[`Content-Type`] shouldBe Some(`Content-Type`(`application/json`))
        val json = entityAs[String]
        decode[HealthInfo](json).map { result =>
          result.keys shouldBe BuildInfo.toMap.keys

        }
      }
  }

}
