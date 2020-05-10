package io.github.mvillafuertem.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.schibsted.spt.data.jslt.Parser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class JsltApplicationSpec extends AnyFlatSpec with Matchers {

  val mapper = new ObjectMapper()

  behavior of "Jslt"

  it should "flatten" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""
                                      |// JSLT transform which flattens nested objects into flat objects
                                      |//   {"a" : {"b" : 1}} => {"a_b" : 1}
                                      |
                                      |def flatten-object(obj)
                                      |  let flat = {for ($obj) .key : .value if (not(is-object(.value)))}
                                      |
                                      |  let nested = [for ($obj)
                                      |     let outerkey = (.key)
                                      |       [for (flatten-object(array(.value))) {
                                      |         "key" : $outerkey + "." + .key,
                                      |         "value" : if (is-object(.value)) flatten-object(.value) else .value
                                      |       }]
                                      |     if (is-object(.value))]
                                      |
                                      |  let flattened = (flatten($nested))
                                      |
                                      |  $flat + {for ($flattened) .key : .value}
                                      |
                                      |flatten-object(.)
                                      |""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree(
      """{"server.timestamp":"2019-03-18T15:28:10.053Z","attributes.device.manufacturer":"Pointer","attributes.device.model":"PointerCompactFleet","attributes.device.tenantId":848860983001616384,"attributes.device.identifier":"256111","attributes.sensors.di4.feature":"door","features.gnss.type":"Gps","features.gnss.precision":"Unknown","features.gnss.satellites":4,"features.location.type":"Gnss","features.location.latitude":32.127758,"features.location.longitude":34.969875,"features.location.speed":36,"features.location.course":0,"features.di4.status":true,"features.engine.ignition.status":false,"features.mainpower.voltage":11.443,"features.battery.level":39,"features.battery.voltage":5.553,"features.network.type":"Gprs","id":"3771a4d1-477f-459c-9b64-90207e486992","timestamp":"2019-03-18T15:28:07.000Z"}"""
    )
    actual shouldBe expected

  }

  it should "filter" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""{"filter" : .features.location.speed > 1}""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree("""{"filter":true}""")
    actual shouldBe expected

  }

  it should "rename" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString(
      """if (.features.location.speed) { "caracteristicas": { "localizacion" : { "velocidad" : .features.location.speed } } }""".stripMargin
    )

    // W H E N
    val actual        = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree("""{"caracteristicas":{"localizacion":{"velocidad":36}}}""")
    actual shouldBe expected

  }

  it should "rename multiples keys" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString(
      """if (.features.location.speed and .features.location.latitude and .features.location.longitude) { "caracteristicas": { "localizacion" : { "velocidad" : .features.location.speed } + { "latitud": .features.location.latitude} + { "longitud": .features.location.longitude} } }""".stripMargin
    )

    // W H E N
    val actual        = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree("""{"caracteristicas":{"localizacion":{"longitud":34.969875,"latitud":32.127758,"velocidad":36}}}""")
    actual shouldBe expected

  }

  it should "append" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""{ *:. } + { "_context" : { "ttl": 5}}""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree(
      """{"_context":{"ttl":5},"id":"3771a4d1-477f-459c-9b64-90207e486992","timestamp":"2019-03-18T15:28:07.000Z","server":{"timestamp":"2019-03-18T15:28:10.053Z"},"attributes":{"device":{"manufacturer":"Pointer","model":"PointerCompactFleet","tenantId":848860983001616384,"identifier":"256111"},"sensors":{"di4":{"feature":"door"}}},"features":{"gnss":{"type":"Gps","precision":"Unknown","satellites":4},"location":{"type":"Gnss","latitude":32.127758,"longitude":34.969875,"speed":36,"course":0},"di4":{"status":true},"engine":{"ignition":{"status":false}},"mainpower":{"voltage":11.443},"battery":{"level":39,"voltage":5.553},"network":{"type":"Gprs"}}}"""
    )
    actual shouldBe expected

  }

  it should "exclude" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""{* - features : .}""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree(
      """{"id":"3771a4d1-477f-459c-9b64-90207e486992","timestamp":"2019-03-18T15:28:07.000Z","server":{"timestamp":"2019-03-18T15:28:10.053Z"},"attributes":{"device":{"manufacturer":"Pointer","model":"PointerCompactFleet","tenantId":848860983001616384,"identifier":"256111"},"sensors":{"di4":{"feature":"door"}}}}"""
    )
    actual shouldBe expected

  }

  it should "exclude key" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""{"features": { "battery": null, "location": null, *:. } } + { * : .}""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree(
      """{"id":"3771a4d1-477f-459c-9b64-90207e486992","timestamp":"2019-03-18T15:28:07.000Z","server":{"timestamp":"2019-03-18T15:28:10.053Z"},"attributes":{"device":{"manufacturer":"Pointer","model":"PointerCompactFleet","tenantId":848860983001616384,"identifier":"256111"},"sensors":{"di4":{"feature":"door"}}},"features":{"gnss":{"type":"Gps","precision":"Unknown","satellites":4},"di4":{"status":true},"engine":{"ignition":{"status":false}},"mainpower":{"voltage":11.443},"network":{"type":"Gprs"}}}"""
    )
    actual shouldBe expected

  }

  it should "include" in {

    // G I V E N
    val jsonNodeInput = mapper.readTree(msg)
    val jslt          = Parser.compileString("""{ "id": .id, "server": .server}""".stripMargin)

    // W H E N
    val actual = jslt.apply(jsonNodeInput)

    // T H E N
    val expected = mapper.readTree("""{"id":"3771a4d1-477f-459c-9b64-90207e486992","server":{"timestamp":"2019-03-18T15:28:10.053Z"}}""")
    actual shouldBe expected

  }

  lazy val msg: String =
    """
      |{
      |  "id": "3771a4d1-477f-459c-9b64-90207e486992",
      |  "timestamp": "2019-03-18T15:28:07.000Z",
      |  "server": {
      |    "timestamp": "2019-03-18T15:28:10.053Z"
      |  },
      |  "attributes": {
      |    "device": {
      |      "manufacturer": "Pointer",
      |      "model": "PointerCompactFleet",
      |      "tenantId": 848860983001616384,
      |      "identifier": "256111"
      |    },
      |    "sensors": {
      |      "di4": {
      |        "feature": "door"
      |      }
      |    }
      |  },
      |  "features": {
      |    "gnss": {
      |      "type": "Gps",
      |      "precision": "Unknown",
      |      "satellites": 4
      |    },
      |    "location": {
      |      "type": "Gnss",
      |      "latitude": 32.127758,
      |      "longitude": 34.969875,
      |      "speed": 36,
      |      "course": 0
      |    },
      |    "di4": {
      |      "status": true
      |    },
      |    "engine": {
      |      "ignition": {
      |        "status": false
      |      }
      |    },
      |    "mainpower": {
      |      "voltage": 11.443
      |    },
      |    "battery": {
      |      "level": 39,
      |      "voltage": 5.553
      |    },
      |    "network": {
      |      "type": "Gprs"
      |    }
      |  }
      |}
      |""".stripMargin

}
