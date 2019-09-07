package io.github.mvillafuertem.todo.api


import akka.actor.testkit.typed.internal.StubbedLogger
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Logger}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ContentTypes.`application/json`
import org.scalatest.{FlatSpecLike, Matchers}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.circe.Json
import io.github.mvillafuertem.todo.BuildInfo
import io.github.mvillafuertem.todo.configuration.ToDoConfiguration
import io.circe.generic.auto._
import io.circe.parser.{decode, _}
import io.circe.syntax._
import io.github.mvillafuertem.todo.api.ToDoAPI.BuildInfo
import io.github.mvillafuertem.todo.api.ToDoAPI._

/**
 * @author Miguel Villafuerte
 */
final class ToDoAPISpec extends ToDoConfiguration
  with FlatSpecLike
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
        decode[BuildInfo](json).map { result =>
          result.keys shouldBe BuildInfo.toMap.keys

        }
      }
  }

}
