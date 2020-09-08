package io.github.mvillafuertem.json

import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.optics.JsonTraversalPath
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json, ParsingFailure}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.circe.optics.JsonPath._

/**
 * @author Miguel Villafuerte
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

    someThing.map(result => result.someThing shouldBe expectedSomeThing.someThing)
    otherThing.map(result => result.otherThing shouldBe expectedOtherThing.otherThing)

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
    actual.map(result => result shouldBe ("value", SomeThing))

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
    actual.map(result => result shouldBe expected)

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

    implicit val decodeThing: Decoder[Thing] = (c: HCursor) =>
      for {
        foo <- c.field("thing").as[String]
      } yield Seq[Thing](SomeThing).filter(_.toString.equalsIgnoreCase(foo)).head

    // W H E N
    val actual = decode[User]("""{"id": 0,"things":["SomeThing", "OtherThing"]}""")

    // T H E N
    val expected: User = User(0L, Seq(SomeThing, OtherThing))

    actual.map(result => result shouldBe expected)

  }

  it should "encode case class with snake case member name" in {

    // G I V E N
    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
    @ConfiguredJsonCodec case class Auth(accessToken: String, expiresIn: Long)

    // W H E N
    val actual = decode[Auth]("""{"access_token": "L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG","expires_in": 4000}""")

    // T H E N
    val expected: Auth = Auth("L7Re1aQ64oi-Tk3WM1CSz0zAPrF_5_f2gTqOkWujN2jJn8C2gTqOkWujN22gTqOkWujG", 4000)

    actual.map(result => result shouldBe expected)

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

    actual.map(_ shouldBe expected)

  }

}
