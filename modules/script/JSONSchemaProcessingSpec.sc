#!/usr/bin/env amm

import $file.JSONSchemaProcessing
import $ivy.`dev.zio::zio-test-sbt:1.0.7`
import $ivy.`dev.zio::zio-test:1.0.7`
import zio._
import zio.console._
import zio.test.Assertion._
import zio.test._
import zio.test.environment._
import io.circe.parser._

// amm `pwd`/modules/script/JSONSchemaProcessingSpec.sc
JSONSchemaProcessingSpec.main(Array())

object JSONSchemaProcessingSpec extends DefaultRunnableSpec {

  override def spec =
    suite("JSONSchemaProcessingSpec")(
      testM("process input1") {
        for {
          json      <- Task.fromEither(parse(inputSchema1))
          actual      <- JSONSchemaProcessing.JSONSchemaProcessing.process(json)
          expected <- Task.fromEither(parse(expectedSchema1))
        } yield assert(actual)(equalTo(expected))
      },
      testM("process input2") {
        for {
          json      <- Task.fromEither(parse(inputSchema2))
          actual      <- JSONSchemaProcessing.JSONSchemaProcessing.process(json)
          expected <- Task.fromEither(parse(expectedSchema2))
        } yield assert(actual)(equalTo(expected))
      }
    ) @@ TestAspect.timed

  lazy val inputSchema1 =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |
      |  "definitions": {
      |    "address": {
      |      "type": "object",
      |      "properties": {
      |        "street_address": { "type": "string" },
      |        "city":           { "type": "string" },
      |        "state":          { "type": "string" }
      |      },
      |      "required": ["street_address", "city", "state"]
      |    }
      |  },
      |
      |  "type": "object",
      |
      |  "properties": {
      |    "billing_address": { "$ref": "#/definitions/address" },
      |    "shipping_address": { "$ref": "#/definitions/address" }
      |  }
      |}
      |""".stripMargin

  lazy val expectedSchema1 =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "definitions": {
      |    "address": {
      |      "anyOf": [
      |        {
      |          "type": "object",
      |          "properties": {
      |            "street_address": {
      |              "anyOf": [
      |                {
      |                  "type": "string"
      |                },
      |                {
      |                  "type": "null"
      |                }
      |              ]
      |            },
      |            "city": {
      |              "anyOf": [
      |                {
      |                  "type": "string"
      |                },
      |                {
      |                  "type": "null"
      |                }
      |              ]
      |            },
      |            "state": {
      |              "anyOf": [
      |                {
      |                  "type": "string"
      |                },
      |                {
      |                  "type": "null"
      |                }
      |              ]
      |            }
      |          },
      |          "required": [
      |            "street_address",
      |            "city",
      |            "state"
      |          ]
      |        },
      |        {
      |          "type": "null"
      |        }
      |      ]
      |    }
      |  },
      |  "type": "object",
      |  "properties": {
      |    "billing_address": {
      |      "$ref": "#/definitions/address"
      |    },
      |    "shipping_address": {
      |      "$ref": "#/definitions/address"
      |    }
      |  }
      |}
      |""".stripMargin


  lazy val inputSchema2 =
    """
      |{
      |  "type": "object",
      |  "properties": {
      |    "street_address": { "type": "string" },
      |    "city":           { "type": "string" },
      |    "state":          { "type": "string" }
      |  },
      |  "required": ["street_address", "city", "state"]
      |}
      |""".stripMargin

  lazy val expectedSchema2 =
    """
      |{
      |  "anyOf": [
      |    {
      |      "type": "object",
      |      "properties": {
      |        "street_address": {
      |          "anyOf": [
      |            {
      |              "type": "string"
      |            },
      |            {
      |              "type": "null"
      |            }
      |          ]
      |        },
      |        "city": {
      |          "anyOf": [
      |            {
      |              "type": "string"
      |            },
      |            {
      |              "type": "null"
      |            }
      |          ]
      |        },
      |        "state": {
      |          "anyOf": [
      |            {
      |              "type": "string"
      |            },
      |            {
      |              "type": "null"
      |            }
      |          ]
      |        }
      |      },
      |      "required": [
      |        "street_address",
      |        "city",
      |        "state"
      |      ]
      |    },
      |    {
      |      "type": "null"
      |    }
      |  ]
      |}
      |""".stripMargin

}