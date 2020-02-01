package io.github.mvillafuertem.json

import com.github.pathikrit.dijon._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers


final class DijonApplicationSpec extends AnyFlatSpecLike with Matchers {

  behavior of "Dijon"

  it should "parse" in {

    // g i v e n
    val process: String =
      """
        |{
        |  "predicate": "values.bar == true",
        |  "deleteKeys": [
        |    "values.baz",
        |    "values.id"
        |  ],
        |  "renameKeys": {
        |    "values.qux[0]": "qux[0]",
        |    "values.qux[1]": "qux[1]"
        |  },
        |  "modifyValue": {
        |    "name": "Bar"
        |  }
        |}
        |""".stripMargin

    val jsonString: String =
      """
        |  {
        |    "id": "c730433b-082c-4984-9d66-855c243266f0",
        |    "name": "Foo",
        |    "counts": [1, 2, 3],
        |    "values": {
        |      "bar": true,
        |      "baz": 100.001,
        |      "qux": ["a", "b"],
        |      "id": "c778908"
        |    }
        |  }
        |""".stripMargin




    // w h e n
    val actual = json"""$jsonString"""

    println(actual.selectDynamic("values.bar") == true)
    println(actual -- ("values.baz"))
    println(actual -- ("values.id"))
    println(actual.updateDynamic("values.qux[0]")("qux[0]"))
    println(actual.updateDynamic("values.qux[1]")("qux[1]"))


    // t h e n
    val expectedJsonString: String =
      """
        |{
        |  "id": "c730433b-082c-4984-9d66-855c243266f0",
        |  "name": "Bar",
        |  "counts": [1, 2, 3],
        |  "values": {
        |    "bar": true
        |  },
        |  "qux": ["a", "b"]
        |}
        |""".stripMargin




  }

}
