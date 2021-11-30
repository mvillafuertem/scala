package io.github.mvillafuertem.advanced.implicits

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class ImplicitClassSpec extends AnyFlatSpecLike with Matchers {

  behavior of "ImplicitClassSpec"

  it should "implicit are available" in {

    // g i v e n
    import io.github.mvillafuertem.advanced.implicits.ImplicitClass.MyStringOpsAdapter

    // w h e n
    val actual = "Hello".mapToDummy(_.toUpper)

    /**
     * Scala hace esto por nosotros new MyStringOpsAdapter("Hello").mapToDummy(_.toUpper) es mucho mas limpio que usar ImplicitsForExtensionMethods
     */

    // t h e n
    actual shouldBe "dummy"

  }

}
