package io.github.mvillafuertem.facades;

import org.junit.Assert._
import org.junit.Test

class FooSpec {

  @Test def testObj(): Unit =
    assertEquals(42, Foo.bar(41))

}
