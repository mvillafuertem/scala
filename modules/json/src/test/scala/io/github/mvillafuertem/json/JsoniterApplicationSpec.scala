package io.github.mvillafuertem.json
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import io.circe.{ CirceJsoniter, CirceJsoniterFlatten, Json }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets

final class JsoniterApplicationSpec extends AnyFlatSpecLike with Matchers {

  behavior of "Jsoniter"

  it should "parse json string to Map[String, String]" in {

    // g i v e n
    val json: String =
      """
        |  {
        |    "id": "c730433b-082c-4984-9d66-855c243266f0",
        |    "name": "Foo"
        |  }
        |""".stripMargin

    implicit val codec: JsonValueCodec[Map[String, String]] = JsonCodecMaker.make[Map[String, String]](CodecMakerConfig)

    // w h e n
    val actual: Map[String, String] = readFromArray(json.getBytes(StandardCharsets.UTF_8))

    // t h e n
    val expected = Map("id" -> "c730433b-082c-4984-9d66-855c243266f0", "name" -> "Foo")
    actual shouldBe expected

  }

  it should "parse json string to Json" in {

    // g i v e n
    val json: String =
      """
        |  {
        |    "id": "c730433b-082c-4984-9d66-855c243266f0",
        |    "name": "Foo",
        |    "counts": [1, 2, 3],
        |    "values": {
        |      "bar": true,
        |      "baz": 100.001,
        |      "qux": ["a", "b"]
        |    }
        |  }
        |""".stripMargin

    implicit val codec: JsonValueCodec[Json] = CirceJsoniter.codec

    // w h e n
    val actual: Any = readFromArray(json.getBytes(StandardCharsets.UTF_8))

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0")),
      ("name", Json.fromString("Foo")),
      ("counts", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      (
        "values",
        Json.obj(
          ("bar", Json.fromBoolean(true)),
          ("baz", Json.fromDoubleOrNull(100.001)),
          ("qux", Json.arr(Json.fromString("a"), Json.fromString("b")))
        )
      )
    )

    actual shouldBe expected

  }

  it should "parse and flatten json string to Json" in {

    // g i v e n
    val json: String =
      """
        |  {
        |    "id": "c730433b-082c-4984-9d66-855c243266f0",
        |    "name": "Foo",
        |    "counts": [1, 2, 3],
        |    "values": {
        |      "bar": true,
        |      "baz": 100.001,
        |      "qux": ["a", "b"]
        |    }
        |  }
        |""".stripMargin

    implicit val codec: JsonValueCodec[Json] = CirceJsoniter.codec

    // w h e n
    val result: Json = readFromArray(json.getBytes(StandardCharsets.UTF_8))
    val actual       = CirceJsoniterFlatten.flatten(result)

    // t h e n
    val expected = Json.obj(
      ("values.qux.[1]", Json.fromString("b")),
      ("values.qux.[0]", Json.fromString("a")),
      ("values.baz", Json.fromDoubleOrNull(100.001)),
      ("values.bar", Json.fromBoolean(true)),
      ("counts.[2]", Json.fromInt(3)),
      ("counts.[1]", Json.fromInt(2)),
      ("counts.[0]", Json.fromInt(1)),
      ("name", Json.fromString("Foo")),
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0"))
    )

    actual shouldBe expected

  }

  it should "parse and blown up json string to Json" in {

    // g i v e n
    val json: String =
      """
        |{
        |  "id" : "c730433b-082c-4984-9d66-855c243266f0",
        |  "name" : "Foo",
        |  "counts.[0]" : 1,
        |  "counts.[1]" : 2,
        |  "counts.[2]" : 3,
        |  "values.bar" : true,
        |  "values.baz" : 100.001,
        |  "values.qux.[0]" : "a",
        |  "values.qux.[1]" : "b"
        |}
        |""".stripMargin

    implicit val codec: JsonValueCodec[Json] = CirceJsoniter.codec

    // w h e n
    val flattened = readFromArray(json.getBytes(StandardCharsets.UTF_8))
    val actual    = CirceJsoniterFlatten.blowup(flattened)

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0")),
      ("name", Json.fromString("Foo")),
      ("counts", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      (
        "values",
        Json.obj(
          ("bar", Json.fromBoolean(true)),
          ("baz", Json.fromDoubleOrNull(100.001)),
          ("qux", Json.arr(Json.fromString("a"), Json.fromString("b")))
        )
      )
    )

    actual shouldBe expected

  }

  it should "parse and flatten json string to Json with codec" in {

    // g i v e n
    val json: String =
      """
        |  {
        |    "id": "c730433b-082c-4984-9d66-855c243266f0",
        |    "name": "Foo",
        |    "counts": [1, 2, 3],
        |    "values": {
        |      "bar": true,
        |      "baz": 100.001,
        |      "qux": ["a", "b"]
        |    }
        |  }
        |""".stripMargin

    implicit val codec: JsonValueCodec[Json] = CirceJsoniterFlatten.codec

    // w h e n
    val actual: Json = readFromArray(json.getBytes(StandardCharsets.UTF_8))

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0")),
      ("name", Json.fromString("Foo")),
      ("counts.[0]", Json.fromInt(1)),
      ("counts.[1]", Json.fromInt(2)),
      ("counts.[2]", Json.fromInt(3)),
      ("values.bar", Json.fromBoolean(true)),
      ("values.baz", Json.fromDoubleOrNull(100.001)),
      ("values.qux.[0]", Json.fromString("a")),
      ("values.qux.[1]", Json.fromString("b"))
    )

    actual shouldBe expected

  }

}
