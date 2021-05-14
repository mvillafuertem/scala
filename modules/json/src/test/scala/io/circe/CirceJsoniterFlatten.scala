package io.circe

import com.github.plokhotnyuk.jsoniter_scala.core.{ JsonReader, JsonValueCodec, JsonWriter }
import io.circe.Json.{ fromJsonObject, JArray, JBoolean, JNull, JNumber, JObject, JString }

import java.util

object CirceJsoniterFlatten {

  def flatten(blownUp: Json): Json = _flatten(blownUp)

  private def _flatten(json: Json, path: String = ""): Json =
    json match {
      case JObject(value) =>
        value.toIterable.map { case (k, v) => _flatten(v, buildPath(path, k)) }
          .fold(JObject(JsonObject.empty))(_ deepMerge _)

      case JArray(value) =>
        value.zipWithIndex.map { case (j, index) => _flatten(j, buildPath(path, s"[$index]")) }
          .fold(JObject(JsonObject.empty))(_ deepMerge _)

      case _ => JObject(JsonObject((path, json)))
    }

  private def buildPath(path: String, key: String): String =
    if (path.isEmpty) key else s"$path.$key"

  private val ArrayElem = """^\[(\d+)\]$""".r

  // TODO Simplificar proceso, ahora se hace en dos pasos componer los json y merge
  def blowup(flattened: Json): Json =
    flattened match {

      case JObject(tuples) =>
        tuples.toIterable.map { case (k, v) => _blowup(k.split('.'), v) }
          .fold(JObject(JsonObject.empty))(deepMerge)

      case _ => throw new RuntimeException("The type was not expected at this position of the document")
    }

  private def deepMerge(other: Json, that: Json): Json =
    (other.asObject, that.asObject) match {
      case (Some(lhs), Some(rhs)) =>
        fromJsonObject(
          lhs.toIterable.foldLeft(rhs) { case (acc, (key, value)) =>
            rhs(key).fold(acc.add(key, value)) { r =>
              acc(key).fold(acc.add(key, deepMerge(value, r))) { json =>
                if (json.isArray) {
                  val value1 = value.asArray.get.appendedAll(json.asArray.get) // TODO esto no es seguro, buscar otra forma
                  acc.add(key, JArray(value1))
                } else {
                  acc.add(key, deepMerge(value, r))
                }
              }
            }
          }
        )
      case _                      => that
    }

  private def _blowup(keys: Array[String] = Array(), value: Json): Json =
    if (keys.isEmpty) value
    else
      keys.head match {
        case ArrayElem(_) => JArray(Vector(value))
        case key          => JObject(JsonObject(key -> _blowup(keys.tail, value)))
      }

  implicit val codec: JsonValueCodec[Json] = new JsonValueCodec[Json] {

    def decodeValueRecursive(k: String, in: JsonReader, default: Json): Json =
      in.nextToken() match {
        case 'n'                                                                   => in.readNullOrError(default, "expected `null` value")
        case '"'                                                                   => decodeString(in)
        case 'f' | 't'                                                             => decodeBoolean(in)
        case n @ ('0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-') => decodeNumber(in, n)
        case '['                                                                   => if (k.isEmpty) decodeArray(k, in, default) else decodeArray(k.concat("."), in, default)
        case '{'                                                                   => if (k.isEmpty) decodeObject(k, in, default) else decodeObject(k.concat("."), in, default)
        case _                                                                     => in.decodeError("expected JSON value")
      }

    override def decodeValue(in: JsonReader, default: Json): Json = decodeValueRecursive("", in, default)

    private def decodeObject(k: String, in: JsonReader, default: Json): JObject =
      JObject(
        if (in.isNextToken('}')) JsonObject.empty
        else {
          val x = new util.LinkedHashMap[String, Json]
          in.rollbackToken()
          do {
            val key    = in.readKeyAsString()
            val str    = k.concat(key)
            val result = decodeValueRecursive(str, in, default)
            result match {
              case JObject(value) =>
                //x.putAll(result.asObject.get.toMap.asJava) // TODO intentar no usar converters .asJava
                value.toIterable.foreach { case (str, json) => x.put(str, json) }
              case _              => x.put(str, result)
            }
          } while (in.isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
          JsonObject.fromLinkedHashMap(x)
        }
      )

    private def decodeArray(k: String, in: JsonReader, default: Json): JObject =
      JObject(
        if (in.isNextToken(']')) JsonObject.empty
        else {
          val m = new util.LinkedHashMap[String, Json]
          in.rollbackToken()
          var i = 0
          do {
            val str    = k.concat(s"[$i]")
            val result = decodeValueRecursive(str, in, default)
            result match {
              case JObject(value) =>
                //m.putAll(result.asObject.get.toMap.asJava) // TODO intentar no usar converters .asJava
                value.toIterable.foreach { case (str, json) => m.put(str, json) }
              case _              => m.put(str, result)
            }
            i += 1
          } while (in.isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
          JsonObject.fromLinkedHashMap(m)
        }
      )

    override def encodeValue(x: Json, out: JsonWriter): Unit =
      x match {
        case JNull       => out.writeNull()
        case JString(s)  => out.writeVal(s)
        case JBoolean(b) => out.writeVal(b)
        case JNumber(n)  =>
          n match {
            case JsonLong(l) => out.writeVal(l)
            case _           => out.writeVal(n.toDouble)
          }
        case JArray(a)   =>
          out.writeArrayStart()
          val l = a.size
          var i = 0
          while (i < l) {
            val json: Json = a(i)
            encodeValue(json, out)
            i += 1
          }
          out.writeArrayEnd()
        case JObject(o)  =>
          out.writeObjectStart()
          val it = o.toIterable.iterator
          while (it.hasNext) {
            val (k, v) = it.next()
            out.writeKey(k)
            encodeValue(v, out)
          }
          out.writeObjectEnd()
      }

    override def nullValue: Json = Json.Null
  }

  private def decodeNumber(in: JsonReader, n: Byte) =
    JNumber({
      in.rollbackToken()
      val d = in.readDouble()
      val i = d.toInt
      if (i.toDouble == d) JsonLong(i)
      else JsonDouble(d)
    })

  private def decodeBoolean(in: JsonReader) = {
    in.rollbackToken()
    if (in.readBoolean()) Json.True
    else Json.False
  }

  private def decodeString(in: JsonReader) = {
    in.rollbackToken()
    JString(in.readString(null))
  }
}
