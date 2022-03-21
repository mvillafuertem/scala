package io.github.mvillafuertem.json

import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.optics.JsonPath._
import io.circe.parser._
import io.circe.syntax._
import monocle.{ Optional, Traversal }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

/**
 * @author
 *   Miguel Villafuerte
 */
final class CirceApplicationSpec extends AnyFlatSpecLike with Matchers {

  behavior of "Circe"

  it should "parse" in {

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

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

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

  it should "flatten" in {

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

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

    def flatten(combineKeys: (String, String) => String)(value: Json): Json = {
      def flattenToFields(value: Json): Option[Iterable[(String, Json)]] =
        value.asObject.map(
          _.toIterable.flatMap { case (k, v) =>
            flattenToFields(v) match {
              case None         => List(k -> v)
              case Some(fields) =>
                fields.map { case (innerK, innerV) =>
                  combineKeys(k, innerK) -> innerV
                }
            }
          }
        )

      flattenToFields(value).fold(value)(Json.fromFields)
    }

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("c730433b-082c-4984-9d66-855c243266f0")),
      ("name", Json.fromString("Foo")),
      ("counts", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      ("values.bar", Json.fromBoolean(true)),
      ("values.baz", Json.fromDoubleOrNull(100.001)),
      ("values.qux", Json.arr(Json.fromString("a"), Json.fromString("b")))
    )

    flatten(_ + "." + _)(actual) shouldBe expected
  }

  it should "transform string" in {

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

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

    def transform(js: Json, f: String => String): Json = js
      .mapString(f)
      .mapArray(_.map(transform(_, f)))
      .mapObject { obj =>
        val updatedObj = obj.toMap.map { case (k, v) =>
          f(k) -> transform(v, f)
        }
        JsonObject.apply(updatedObj.toSeq: _*)
      }

    // t h e n
    val expected = Json.obj(
      ("ID", Json.fromString("C730433B-082C-4984-9D66-855C243266F0")),
      ("NAME", Json.fromString("FOO")),
      ("COUNTS", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      (
        "VALUES",
        Json.obj(
          ("BAR", Json.fromBoolean(true)),
          ("BAZ", Json.fromDoubleOrNull(100.001)),
          ("QUX", Json.arr(Json.fromString("A"), Json.fromString("B")))
        )
      )
    )

    transform(actual, s => s.toUpperCase) shouldBe expected
  }

  it should "transform string values" in {

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

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

    def transformStringValues(json: Json, f: String => String): Json = json
      .mapString(f)
      .mapArray(a => a.map(transformStringValues(_, f)))
      .mapObject(obj => JsonObject(obj.toMap.view.mapValues(transformStringValues(_, f)).toSeq: _*))

    // t h e n
    val expected = Json.obj(
      ("id", Json.fromString("C730433B-082C-4984-9D66-855C243266F0")),
      ("name", Json.fromString("FOO")),
      ("counts", Json.arr(Json.fromInt(1), Json.fromInt(2), Json.fromInt(3))),
      (
        "values",
        Json.obj(
          ("bar", Json.fromBoolean(true)),
          ("baz", Json.fromDoubleOrNull(100.001)),
          ("qux", Json.arr(Json.fromString("A"), Json.fromString("B")))
        )
      )
    )

    transformStringValues(actual, s => s.toUpperCase) shouldBe expected
  }

  it should "encode class" in {

    // G I V E N
    class Thing(val foo: String, val bar: Int)

    implicit val encodeThing: Encoder[Thing] = (a: Thing) =>
      Json.obj(
        ("foo", Json.fromString(a.foo)),
        ("bar", Json.fromInt(a.bar))
      )

    // W H E N
    val actual: Json = new Thing("a", 1).asJson

    // T H E N
    val expected = Json.obj(
      ("foo", Json.fromString("a")),
      ("bar", Json.fromInt(1))
    )

    actual shouldBe expected

  }

  it should "decode class" in {

    // G I V E N
    class Thing(val foo: String, val bar: Int)

    implicit val decodeThing: Decoder[Thing] = (c: HCursor) =>
      for {
        foo <- c.downField("foo").as[String]
        bar <- c.downField("bar").as[Int]
      } yield new Thing(foo, bar)

    // W H E N
    val actual = decode[Thing]("""{"foo":"a","bar":1}""")

    // T H E N
    val expected = new Thing("a", 1)

    actual.isRight shouldBe true
    actual.map { result =>
      result.bar shouldBe expected.bar
      result.foo shouldBe expected.foo
    }

  }

  it should "encode sealed trait" in {

    // G I V E N
    sealed trait Thing
    case class SomeThing(someThing: String) extends Thing
    case class OtherThing(otherThing: Int)  extends Thing

    implicit val encodeThing: Encoder[Thing] = Encoder.instance {
      case someThing @ SomeThing(_)   => someThing.asJson
      case otherThing @ OtherThing(_) => otherThing.asJson
    }

    // W H E N
    val someThing  = SomeThing("a").asJson
    val otherThing = OtherThing(1).asJson

    // T H E N
    val expectedSomeThing  = Json.obj(
      ("someThing", Json.fromString("a"))
    )
    val expectedOtherThing = Json.obj(
      ("otherThing", Json.fromInt(1))
    )

    someThing shouldBe expectedSomeThing
    otherThing shouldBe expectedOtherThing

  }

  it should "decode sealed trait" in {

    // G I V E N
    sealed trait Thing
    case class SomeThing(someThing: String) extends Thing
    case class OtherThing(otherThing: Int)  extends Thing

    implicit val decodeThing: Decoder[Thing] =
      Decoder[SomeThing]
        .map[Thing](identity)
        .or(Decoder[OtherThing].map[Thing](identity))

    // W H E N
    val someThing  = decode[SomeThing]("""{"someThing":"a"}""")
    val otherThing = decode[OtherThing]("""{"otherThing":1}""")

    // T H E N
    val expectedSomeThing  = SomeThing("a")
    val expectedOtherThing = OtherThing(1)

    someThing shouldBe Right(expectedSomeThing)
    otherThing shouldBe Right(expectedOtherThing)

  }

  it should "encode sealed trait with object" in {

    // G I V E N
    sealed trait Thing
    case object SomeThing extends Thing

    implicit val encodeThing: Encoder[Thing] = (a: Thing) =>
      Json.obj(
        ("value", Json.fromString(a.toString))
      )

    // W H E N
    val actual: Thing = SomeThing

    // T H E N
    val expected = Json.obj(
      ("value", Json.fromString("SomeThing"))
    )

    actual.asJson shouldBe expected

  }

  it should "decode sealed trait with object" in {

    // G I V E N
    sealed trait Thing
    case object SomeThing extends Thing
    type SomeThingResult = (String, Thing)

    implicit val decodeThing: Decoder[SomeThingResult] = (c: HCursor) =>
      for {
        foo <- c.downField("value").as[String]
      } yield new SomeThingResult("value", Seq[Thing](SomeThing).filter(_.toString.equalsIgnoreCase(foo)).head)

    // W H E N
    val actual = decode[SomeThingResult]("""{"value":"SomeThing"}""")

    // T H E N
    actual shouldBe Right(("value", SomeThing))

  }

  it should "encode case class with sealed trait with object" in {

    // G I V E N
    case class User(id: Long, thing: Thing)
    sealed trait Thing
    case object SomeThing extends Thing

    implicit val encodeThing: Encoder[Thing] = (a: Thing) => Json.fromString(a.toString)

    // W H E N
    val actual: User = User(0L, SomeThing)

    // T H E N
    val expected = Json.obj(
      ("id", Json.fromInt(0)),
      ("thing", Json.fromString("SomeThing"))
    )

    actual.asJson shouldBe expected

  }

  it should "decode case class with sealed trait with object" in {

    // G I V E N
    case class User(id: Long, thing: Thing)
    sealed trait Thing
    case object SomeThing extends Thing

    implicit val decodeThing: Decoder[Thing] = (c: HCursor) =>
      for {
        foo <- c.field("thing").as[String]
      } yield Seq[Thing](SomeThing).filter(_.toString.equalsIgnoreCase(foo)).head

    // W H E N
    val actual = decode[User]("""{"id": 0,"thing":"SomeThing"}""")

    // T H E N
    val expected: User = User(0L, SomeThing)

    // T H E N
    actual shouldBe Right(expected)

  }

  it should "encode case class with sequence of sealed trait with object" in {

    // G I V E N
    case class User(id: Long, thing: Seq[Thing])
    sealed trait Thing
    case object SomeThing  extends Thing
    case object OtherThing extends Thing

    implicit val encodeThing: Encoder[Thing] = (a: Thing) => Json.fromString(a.toString)

    // W H E N
    val actual: User = User(0L, Seq(SomeThing, OtherThing))

    // T H E N
    val expected = Json.obj(
      ("id", Json.fromInt(0)),
      ("thing", Json.arr(Json.fromString("SomeThing"), Json.fromString("OtherThing")))
    )

    actual.asJson shouldBe expected

  }

  it should "decode case class with sequence of sealed trait with object" in {

    // G I V E N
    case class User(id: Long, thing: Seq[Thing])
    sealed trait Thing
    case object SomeThing  extends Thing
    case object OtherThing extends Thing

    implicit val decodeThing: Decoder[Seq[Thing]] = (c: HCursor) =>
      for {
        foo <- c.field("thing").as[Seq[String]]
      } yield foo.flatMap(f => Seq[Thing](SomeThing, OtherThing).filter(_.toString.equalsIgnoreCase(f)))

    // W H E N
    val actual = decode[User]("""{"id": 0,"thing":["SomeThing", "OtherThing"]}""")

    // T H E N
    val expected: User = User(0L, Seq(SomeThing, OtherThing))

    actual shouldBe Right(expected)

  }

  it should "decode case class with sequence of sealed trait with object using decoderHCursor method" in {

    // G I V E N
    case class User(id: Long, thing: Seq[Thing])
    sealed trait Thing
    case object SomeThing  extends Thing
    case object OtherThing extends Thing

    implicit val decodeThing: Decoder[Seq[Thing]] = Decoder.decodeHCursor.emap { hcursor =>
      hcursor.field("thing").as[Seq[String]].left.map(_.message).map(_.flatMap(f => Seq[Thing](SomeThing, OtherThing).filter(_.toString.equalsIgnoreCase(f))))
    }

    // W H E N
    val actual = decode[User]("""{"id": 0,"thing":["SomeThing", "OtherThing"]}""")

    // T H E N
    val expected: User = User(0L, Seq(SomeThing, OtherThing))

    actual shouldBe Right(expected)

  }

  it should "encode case class with snake case member name" in {

    // G I V E N
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
    @ConfiguredJsonCodec case class Auth(accessToken: String, expiresIn: Long)

    // W H E N
    val actual = decode[Auth]("""{"access_token": "L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG","expires_in": 4000}""")

    // T H E N
    val expected: Auth = Auth("L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG", 4000)

    actual shouldBe Right(expected)

  }

  it should "encode case class with snake case member name no annotations" in {

    // G I V E N
    import io.circe.generic.extras.semiauto.deriveConfiguredCodec

    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
    case class Auth(accessToken: String, expiresIn: Long)

    implicit val decisionElementCodec: Codec[Auth] = deriveConfiguredCodec

    // W H E N
    val actual = decode[Auth]("""{"access_token": "L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG","expires_in": 4000}""")

    // T H E N
    val expected: Auth = Auth("L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG", 4000)

    actual shouldBe Right(expected)

  }

  it should "semiauto encoder case object of role" in {

    // G I V E N
    import io.circe.Codec
    import io.circe.generic.extras.Configuration
    import io.circe.generic.extras.semiauto.deriveEnumerationCodec

    case class Person(name: String, role: Role)
    sealed trait Role
    case object User extends Role

    implicit val config: Configuration  = Configuration.default.copy(transformConstructorNames = _.toLowerCase)
    implicit val roleCodec: Codec[Role] = deriveEnumerationCodec[Role]

    // W H E N
    val actual = Person("Pepe", User).asJson.noSpaces

    // T H E N
    actual shouldBe """{"name":"Pepe","role":"user"}"""

  }

  it should "semiauto encoder case object of role with defaults" in {

    // G I V E N
    import io.circe.Codec
    import io.circe.generic.extras.Configuration
    import io.circe.generic.extras.semiauto.{ deriveConfiguredCodec, deriveEnumerationCodec }

    case class Person(name: String = "", role: Role = User, permission: Seq[String] = Nil)
    sealed trait Role
    case object User extends Role

    implicit val config: Configuration               = Configuration.default.copy(transformConstructorNames = _.toLowerCase).withDefaults
    implicit val roleCodec: Codec[Role]              = deriveEnumerationCodec[Role]
    implicit val decisionElementCodec: Codec[Person] = deriveConfiguredCodec

    // W H E N
    val actual = decode[Person]("""{}""")

    // T H E N
    actual shouldBe Right(Person())

  }

  it should "manual encoder case object" in {

    // G I V E N
    case class Person(name: String, role: Role)
    sealed trait Role
    case object User extends Role

    implicit val decodeMode: Decoder[Role] = Decoder[String].emap {
      case "user" => Right(User)
      case other  => Left(s"Invalid role: $other")
    }

    implicit val encodeMode: Encoder[Role] = Encoder[String].contramap { case User =>
      "user"
    }

    // W H E N
    val actual = Person("Pepe", User).asJson.noSpaces

    // T H E N
    actual shouldBe """{"name":"Pepe","role":"user"}"""

  }

  // @see https://stackoverflow.com/questions/65899574/scala-circe-deriveunwrapped-value-class-doesnt-work-for-missing-member/65905438#65905438
  it should "manual encoder empty string" in {

    // G I V E N
    import io.circe.generic.extras.semiauto.{ deriveUnwrappedDecoder, deriveUnwrappedEncoder }

    final case class CustomString(value: Option[String])
    final case class TestString(name: CustomString)

    implicit val customStringDecoder: Decoder[CustomString] =
      Decoder
        .decodeOption(deriveUnwrappedDecoder[CustomString])
        .map(ssOpt => CustomString(ssOpt.flatMap(_.value.flatMap(s => Option.when(s.nonEmpty)(s)))))
    implicit val customStringEncoder: Encoder[CustomString] = deriveUnwrappedEncoder[CustomString]
    implicit val testStringCodec: Codec[TestString]         = io.circe.generic.semiauto.deriveCodec

    // W H E N
    val testString      = TestString(CustomString(Some("test")))
    val emptyTestString = TestString(CustomString(Some("")))
    val noneTestString  = TestString(CustomString(None))
    val nullJson        = """{"name":null}"""
    val emptyJson       = """{}"""

    // T H E N
    assert(testString.asJson.noSpaces == """{"name":"test"}""")
    assert(emptyTestString.asJson.noSpaces == """{"name":""}""")
    assert(noneTestString.asJson.noSpaces == nullJson)
    assert(noneTestString.asJson.dropNullValues.noSpaces == emptyJson)

    assert(decode[TestString](nullJson).exists(_ == noneTestString))  // this passes
    assert(decode[TestString](emptyJson).exists(_ == noneTestString)) // this fails

  }

  it should "deep merge" in {

    // G I V E N
    val jsonA =
      """
        |{
        |  "values" : {
        |    "qux" : [
        |      "a"
        |    ]
        |  }
        |}
        |""".stripMargin
    val jsonB =
      """
        |{
        |  "values" : {
        |    "qux" : [
        |      "b"
        |    ]
        |  }
        |}
        |""".stripMargin

    // W H E N
    def deepMerge(other: Json, that: Json): Json =
      (other.asObject, that.asObject) match {
        case (Some(lhs), Some(rhs)) =>
          Json.fromJsonObject(
            lhs.toIterable.foldLeft(rhs) { case (acc, (key, value)) =>
              rhs(key).fold(acc.add(key, value)) { r =>
                acc(key).fold(acc.add(key, deepMerge(value, r))) { json =>
                  if (json.isArray) {
                    val value1 = value.asArray.get.appendedAll(json.asArray.get) // TODO esto no es seguro, buscar otra forma
                    acc.add(key, value1.asJson)
                  } else {
                    acc.add(key, deepMerge(value, r))
                  }
                }
              }
            }
          )
        case _                      => that
      }

    val actual = for {
      a <- parse(jsonA)
      b <- parse(jsonB)
    } yield deepMerge(a, b)

    // T H E N
    val expected = parse("""
                           |{
                           |  "values" : {
                           |    "qux" : [
                           |      "a",
                           |      "b"
                           |    ]
                           |  }
                           |}
                           |""".stripMargin)

    actual shouldBe expected

  }

  it should "use of optics" in {

    // G I V E N
    val jsonString =
      """{"data":[{"type":"gif","id":"OkJat1YNdoD3W","url":"https://giphy.com/gifs/welcome-OkJat1YNdoD3W","username":"","source":"https://dribbble.com/shots/2432051-Welcome-Cel-Animation","title":"welcome GIF","rating":"g","content_url":"","tags":[],"featured_tags":[],"user_tags":[],"source_tld":"dribbble.com","source_post_url":"https://dribbble.com/shots/2432051-Welcome-Cel-Animation","is_sticker":0,"import_datetime":"2016-09-15 01:34:56","trending_datetime":"0000-00-00 00:00:00","images":{"downsized_medium":{"height":"360","width":"480","size":"870428","url":"https://media1.giphy.com/media/OkJat1YNdoD3W/giphy.gif?cid=641ed420f21ue9qvz3ypteu88nwiopwna6ir3xqe41y1ywrm&rid=giphy.gif"}}},{"type":"gif","id":"l46Cpz0A0dB1jMxG0","url":"https://giphy.com/gifs/welcome-the-nanny-fran-drescher-l46Cpz0A0dB1jMxG0","username":"","source":"","title":"The Nanny Jewish GIF","rating":"g","content_url":"","tags":[],"featured_tags":[],"user_tags":[],"source_tld":"","source_post_url":"","is_sticker":0,"import_datetime":"2016-07-26 19:25:04","trending_datetime":"2019-11-15 19:15:09","images":{"downsized_medium":{"height":"318","width":"480","size":"977304","url":"https://media0.giphy.com/media/l46Cpz0A0dB1jMxG0/giphy.gif?cid=641ed420f21ue9qvz3ypteu88nwiopwna6ir3xqe41y1ywrm&rid=giphy.gif"}}}],"pagination":{"total_count":7610,"count":2,"offset":0},"meta":{"status":200,"msg":"OK","response_id":"f21ue9qvz3ypteu88nwiopwna6ir3xqe41y1ywrm"}}"""
    case class Gif(id: String, title: String, url: String)

    // W H E N
    val actual: Either[ParsingFailure, List[Gif]] = parse(jsonString)
      .map(root.data.each.json.getAll)
      .map(
        _.map(json =>
          Gif(
            root.id.string.getOption(json).getOrElse(""),
            root.title.string.getOption(json).getOrElse(""),
            root.images.downsized_medium.url.string.getOption(json).getOrElse("")
          )
        )
      )

    // T H E N
    val expected = List(
      Gif(
        "OkJat1YNdoD3W",
        "welcome GIF",
        "https://media1.giphy.com/media/OkJat1YNdoD3W/giphy.gif?cid=641ed420f21ue9qvz3ypteu88nwiopwna6ir3xqe41y1ywrm&rid=giphy.gif"
      ),
      Gif(
        "l46Cpz0A0dB1jMxG0",
        "The Nanny Jewish GIF",
        "https://media0.giphy.com/media/l46Cpz0A0dB1jMxG0/giphy.gif?cid=641ed420f21ue9qvz3ypteu88nwiopwna6ir3xqe41y1ywrm&rid=giphy.gif"
      )
    )

    actual shouldBe Right(expected)

  }

  it should "schema" in {

    // g i v e n
    val json: String =
      """
        |{
        |  "$id": "https://example.com/person.schema.json",
        |  "$schema": "https://json-schema.org/draft/2020-12/schema",
        |  "title": "Person",
        |  "type": "object",
        |  "properties": {
        |    "firstName": {
        |      "type": "string",
        |      "description": "The person's first name."
        |    },
        |    "lastName": {
        |      "type": "string",
        |      "description": "The person's last name."
        |    },
        |    "age": {
        |      "description": "Age in years which must be equal to or greater than zero.",
        |      "type": "integer",
        |      "minimum": 0
        |    }
        |  }
        |}
        |""".stripMargin

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

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

    // t h e n
    val expected = parse("""
                           |{
                           |  "$id": "https://example.com/person.schema.json",
                           |  "$schema": "https://json-schema.org/draft/2020-12/schema",
                           |  "title": "Person",
                           |  "type": "object",
                           |  "properties": {
                           |    "firstName": {
                           |      "anyOf": [
                           |        {
                           |          "type": "string",
                           |          "description": "The person's first name."
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    },
                           |    "lastName": {
                           |      "anyOf": [
                           |        {
                           |          "type": "string",
                           |          "description": "The person's last name."
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    },
                           |    "age": {
                           |      "anyOf": [
                           |        {
                           |          "description": "Age in years which must be equal to or greater than zero.",
                           |          "type": "integer",
                           |          "minimum": 0
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    }
                           |  }
                           |}
                           |""".stripMargin).getOrElse(Json.Null)

    schema(actual) shouldBe expected
  }

  it should "schemaFold" in {

    // g i v e n
    val json: String =
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

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

    def schemaFold(that: Json): Json = {
      def recursive(that: Json): Json =
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
    }

    // t h e n
    val expected = parse("""
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
                           |""".stripMargin).getOrElse(Json.Null)

    schemaFold(actual) shouldBe expected
  }

  it should "schemaFolder" in {

    // g i v e n
    val json: String =
      """
        |{
        |  "$id": "https://example.com/person.schema.json",
        |  "$schema": "https://json-schema.org/draft/2020-12/schema",
        |  "title": "Person",
        |  "type": "object",
        |  "properties": {
        |    "firstName": {
        |      "type": "string",
        |      "description": "The person's first name."
        |    },
        |    "lastName": {
        |      "type": "string",
        |      "description": "The person's last name."
        |    },
        |    "age": {
        |      "description": "Age in years which must be equal to or greater than zero.",
        |      "type": "integer",
        |      "minimum": 0
        |    }
        |  }
        |}
        |""".stripMargin

    // w h e n
    val actual: Json = parse(json).getOrElse(Json.Null)

    def schemaFolder(json: Json): Json = {
      val folder = new Json.Folder[Json] {
        def onNull: Json = Json.Null

        def onBoolean(value: Boolean): Json = Json.fromBoolean(value)

        def onNumber(value: JsonNumber): Json = Json.fromJsonNumber(value)

        def onString(value: String): Json = Json.fromString(value)

        def onArray(value: Vector[Json]): Json = Json.fromValues(value)

        def onObject(value: JsonObject): Json =
          if (value.contains("type") && !value.contains("$schema")) {
            Json.fromJsonObject(
              JsonObject(
                ("anyOf", Json.arr(JsonObject(value.toMap.view.mapValues(schemaFolder).toSeq: _*).asJson, Json.obj(("type", Json.fromString("null")))))
              )
            )
          } else {
            if (!value.asJson.isObject) {
              Json.fromJsonObject(value)
            } else {
              val updatedObj = value.toMap.map { case (key, value) => key -> schemaFolder(value) }
              Json.fromJsonObject(updatedObj.asJsonObject)
            }
          }
      }

      json.foldWith(folder)
    }

    // t h e n
    val expected = parse("""
                           |{
                           |  "$id": "https://example.com/person.schema.json",
                           |  "$schema": "https://json-schema.org/draft/2020-12/schema",
                           |  "title": "Person",
                           |  "type": "object",
                           |  "properties": {
                           |    "firstName": {
                           |      "anyOf": [
                           |        {
                           |          "type": "string",
                           |          "description": "The person's first name."
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    },
                           |    "lastName": {
                           |      "anyOf": [
                           |        {
                           |          "type": "string",
                           |          "description": "The person's last name."
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    },
                           |    "age": {
                           |      "anyOf": [
                           |        {
                           |          "description": "Age in years which must be equal to or greater than zero.",
                           |          "type": "integer",
                           |          "minimum": 0
                           |        },
                           |        {
                           |          "type": "null"
                           |        }
                           |      ]
                           |    }
                           |  }
                           |}
                           |""".stripMargin).getOrElse(Json.Null)

    schemaFolder(actual) shouldBe expected
  }

  it should "extract tuples of values from json" in {

    // g i v e n
    val jsonFileName = "/sample.json"
    val jsonString   = scala.io.Source.fromInputStream(getClass.getResourceAsStream(jsonFileName)).mkString

    // w h e n
    val dataField: Traversal[Json, Json]   = root.data.each.json
    val guidField: Optional[Json, String]  = root.guid.string
    val nameField: Optional[Json, String]  = root.name.string
    val friendsField: Optional[Json, Json] = root.friends.json

    // t h e n
    val actual: Either[ParsingFailure, Json] = parse(jsonString)
      .map(dataField.getAll)
      .map(
        _.filter(json => nameField.getOption(json).fold(false)(_ == "Wade Jordan"))
          .map(json =>
          guidField
            .getOption(json)
            .fold[(String, Json)]("null" -> Json.Null)(guid =>
                guid -> Json.obj(
                  "name"    -> nameField.getOption(json).fold(Json.Null)(a => Json.fromString(a)),
                  "friends" -> friendsField.getOption(json).fold(Json.Null)(identity)
                )
            )
        )
      )
      .map(Json.obj(_: _*))

    actual.isRight shouldBe true
    actual shouldBe parse("""{"68a346f6-873b-4c07-b808-9cd446a6f18a":{"name":"Wade Jordan","friends":[{"id":0,"name":"Duke Estes"},{"id":1,"name":"Kim Hayes"},{"id":2,"name":"Branch Chang"}]}}""".stripMargin)

  }

}
