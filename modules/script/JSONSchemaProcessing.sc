#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.4`
import $ivy.`dev.zio::zio:1.0.9`
import $ivy.`io.circe::circe-core:0.14.1`
import $ivy.`io.circe::circe-generic:0.14.1`
import $ivy.`io.circe::circe-parser:0.14.1`
import $ivy.`org.slf4j:slf4j-api:1.7.30`

import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.circe.{ Json, JsonObject }
import org.slf4j.{ Logger, LoggerFactory }
import zio.console._
import zio.{ ExitCode, Task, UIO, URIO, ZIO }
import zio.ZIO._

import java.io.{ File, FileInputStream }
import java.nio.charset.StandardCharsets

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// echo "{\"priority\":{\"type\":\"string\"}}" > input-schema.json
// amm `pwd`/JSONSchemaProcessing.sc input-schema.json
@main
def main(schema: String): Unit = JSONSchemaProcessing.main(Array(schema))

object JSONSchemaProcessing extends zio.App {

  def process(json: Json): Task[Json] = Task.effect {
    def schema(json: Json): Json = json.mapObject { obj =>
      if (obj.contains("type") && !obj.contains("$schema")) {
        val updatedObj = obj.toMap.map { case (key, value) => key -> schema(value) }
        JsonObject(("anyOf", Json.arr(updatedObj.asJson, Json.obj(("type", Json.fromString("null"))))))
      } else {
        if (!obj.asJson.isObject) {
          obj
        } else {
          val updatedObj = obj.toMap.map { case (key, value) => key -> schema(value) }
          updatedObj.asJsonObject
        }
      }
    }
    schema(json)
  }

  def process2(json: Json): Task[Json] = Task.effect {
    def schema2(json: Json): Json = json.mapObject { obj =>
      if (obj.contains("type") && !obj.contains("$schema")) {
        val updatedObj = JsonObject(obj.toMap.view.mapValues(schema2).toSeq: _*).asJson
        JsonObject(("anyOf", Json.arr(updatedObj, Json.obj(("type", Json.fromString("null"))))))
      } else {
        if (!obj.asJson.isObject) {
          obj
        } else {
          JsonObject(obj.toMap.view.mapValues(schema2).toSeq: _*)
        }
      }
    }
    schema2(json)
  }
  def process3(json: Json): Task[Json] = Task.effect {
    def recursive(that: Json): Json  =
      that.asObject match {
        case Some(value) =>
          value.toIterable
            .foldLeft(JsonObject.empty) { case (acc, (key, value)) =>
              if (value.isObject) {
                val obj = value.asObject.get
                if (obj.contains("type") && !obj.contains("$schema")) {
                  acc.add(
                    key,
                    Json.obj(
                      ("anyOf", Json.arr(recursive(obj.asJson), Json.obj(("type", Json.fromString("null")))))
                    )
                  )
                } else {
                  acc.add(key, recursive(value))
                }
              } else {
                acc.add(key, value)
              }
            }
            .asJson
        case None        => that
      }
    def schemaFold(that: Json): Json =
      that.asObject match {
        case Some(value) if value.contains("type") && !value.contains("$schema") =>
          Json.obj(
            (
              "anyOf",
              Json.arr(value.mapValues(recursive).asJson, Json.obj(("type", Json.fromString("null"))))
            )
          )
        case Some(value)                                                         => recursive(value.asJson)
        case None                                                                => that
      }
    schemaFold(json)
  }

  def readFile(path: String): Task[FileInputStream] =
    Task.effect(new FileInputStream(new File(path)))

  def closeFile(is: FileInputStream): UIO[Unit] =
    UIO.effectTotal(is.close())

  def processSchema(is: FileInputStream): ZIO[Console, Throwable, Unit] =
    for {
      bytes  <- Task.effect(is.readAllBytes())
      json   <- Task.fromEither(parse(new String(bytes, StandardCharsets.UTF_8)))
      result <- process(json)
      _      <- ZIO.debug(result.spaces2)
    } yield ()

  def program(args: List[String]): ZIO[Console, Throwable, Unit] =
    readFile(args.head)
      .bracket(closeFile)(processSchema)
      .onError(ex => ZIO.debug(s"Failed to read file: ${ex.failures}"))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program(args).fold(_ => ExitCode.failure, _ => ExitCode.success)

}

/**
 * original code augment.jq
 * bash$ jq -f augment.jq input-schema.json
 *
 * def has_field(field):
 *  type == "object" and has(field);
 *
 * def this_or_null:
 *  if has_field("type") and (has_field("$schema") | not)
 *  then
 *    {
 *      anyOf: [
 *        .,
 *        {
 *          type: "null"
 *        }
 *      ]
 *    }
 *  else
 *    .
 *  end;
 *
 * def make_nullable_recursively(is_already):
 *  if type != "object"
 *  then
 *    .
 *  else
 *    with_entries(
 *      {
 *        key: .key,
 *        value: (.value | make_nullable_recursively(has_field("anyOf")) | if is_already then . else this_or_null end)
 *      }
 *    ) | this_or_null
 *  end;
 *
 * make_nullable_recursively(has_field("anyOf"))
 */
