package io.github.mvillafuertem.basic.map

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class MapSpec extends AnyFlatSpecLike with Matchers {

  behavior of "Map"

  it should "group map reduce by key" in {

    // g i v e n
    val mapSequence = Seq(
      Map("predicate" -> true),
      Map("predicate" -> false),
      Map("predicate" -> true),
      Map("predicate" -> false),
      Map("name"      -> "false")
    )

    // w h e n
    val actual = mapSequence.groupMapReduce(_.keys.head)(_.values.toSeq)(_ ++ _)

    // t h e n
    actual shouldBe Map("predicate" -> Seq(true, false, true, false), "name" -> Seq("false"))

  }

  it should "group map reduce by key and type" in {

    // g i v e n
    val mapSequence = Seq(
      Map("predicate" -> true),
      Map("predicate" -> false),
      Map("predicate" -> true),
      Map("predicate" -> false),
      Map("name"      -> "false")
    )

    // w h e n
    val actual = mapSequence.foldLeft(Map.empty[String, Any]) {
      case (a: Map[String, Any], b: Map[String, Any]) =>
        if (a.contains(b.keys.head)) {
          a.filter { case (k, v) => k.equals(b.keys.head) }.map { case (k, v) => k -> b.values.toSeq.appended(v) }
        } else {
          a ++ b
        }
    }

    // t h e n
    actual shouldBe Map("predicate" -> List(false, List(true, List(false, true))), "name" -> "false")

  }

}
