package io.circe

import com.github.plokhotnyuk.jsoniter_scala.core.{ JsonReader, JsonReaderException, JsonValueCodec, JsonWriter }
import io.circe.Json.{ JArray, JBoolean, JNull, JNumber, JObject, JString }

import java.util
import scala.collection.mutable

object CirceJsoniter {

  implicit val codec: JsonValueCodec[Json] = new JsonValueCodec[Json] {

    override def decodeValue(in: JsonReader, default: Json): Json =
      in.nextToken() match {
        case 'n'                                                                   => in.readNullOrError(default, "expected `null` value")
        case '"'                                                                   =>
          in.rollbackToken()
          JString(in.readString(null))
        case 'f' | 't'                                                             =>
          in.rollbackToken()
          if (in.readBoolean()) Json.True
          else Json.False
        case n @ ('0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-') =>
          JNumber({
            in.rollbackToken()
            val d = in.readDouble()
            val i = d.toInt
            if (i.toDouble == d) JsonLong(i)
            else JsonDouble(d)
          })
        case '['                                                                   =>
          JArray {
            val arr = new mutable.ArrayBuffer[Json](4)
            if (!in.isNextToken(']')) {
              in.rollbackToken()
              do arr += decodeValue(in, default) while (in.isNextToken(','))
              if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
            }
            arr.toVector
          }
        case '{'                                                                   =>
          JObject(
            if (in.isNextToken('}')) JsonObject.empty
            else {
              val x = new util.LinkedHashMap[String, Json]
              in.rollbackToken()
              do x.put(in.readKeyAsString(), decodeValue(in, default)) while (in.isNextToken(','))
              if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
              JsonObject.fromLinkedHashMap(x)
            }
          )
        case _                                                                     => in.decodeError("expected JSON value")
      }

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

}
