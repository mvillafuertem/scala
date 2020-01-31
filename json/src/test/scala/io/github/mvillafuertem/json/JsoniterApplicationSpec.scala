package io.github.mvillafuertem.json
import java.nio.charset.StandardCharsets

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import io.circe.{CirceJsoniter, CirceJsoniterFlatten, Json}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

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
      ("values",
        Json.obj(
          ("bar", Json.fromBoolean(true)),
          ("baz", Json.fromDoubleOrNull(100.001)),
          ("qux", Json.arr(Json.fromString("a"), Json.fromString("b"))),
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

    implicit val codec: JsonValueCodec[Json] = CirceJsoniterFlatten.codec


    // w h e n
    val actual: Any = readFromArray(json.getBytes(StandardCharsets.UTF_8))

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0")),
      ("name", Json.fromString("Foo")),
      ("counts", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      ("values",
        Json.obj(
          ("bar", Json.fromBoolean(true)),
          ("baz", Json.fromDoubleOrNull(100.001)),
          ("qux", Json.arr(Json.fromString("a"), Json.fromString("b"))),
        )
      )
    )

    actual shouldBe expected

  }

}


