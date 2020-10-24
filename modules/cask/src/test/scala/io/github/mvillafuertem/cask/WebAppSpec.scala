package io.github.mvillafuertem.cask

import org.scalatest.funsuite.AnyFunSuite

final class WebAppSpec extends AnyFunSuite {
  test("hello returns 'Hello World!'") {
    assert(WebApp.hello() === "Hello World!")
  }
}
