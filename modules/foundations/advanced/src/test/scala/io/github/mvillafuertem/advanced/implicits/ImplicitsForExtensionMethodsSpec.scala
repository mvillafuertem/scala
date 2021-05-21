package io.github.mvillafuertem.advanced.implicits

import io.github.mvillafuertem.advanced.implicits.ImplicitsForExtensionMethods.MyStringOpsAdapter
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ImplicitsForExtensionMethodsSpec extends AnyFlatSpecLike with Matchers {

  behavior of "ImplicitsForExtensionMethodsSpec"

  it should "implicit are not available" in {

    // g i v e n
    // w h e n
    val actual = new MyStringOpsAdapter("Hello").mapToDummy(_.toUpper)

    // t h e n
    actual shouldBe "dummy"

  }

  it should "implicit are available" in {

    // g i v e n
    import io.github.mvillafuertem.advanced.implicits.ImplicitsForExtensionMethods._
    // w h e n
    val actual = "Hello".mapToDummy(_.toUpper)

    /**
     * Scala hace esto por nosotros toMyStringOpsAdapter("Hello").mapToDummy(_.toUpper)
     */

    // t h e n
    actual shouldBe "dummy"

  }

}
