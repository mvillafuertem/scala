package io.github.mvillafuertem.advanced

import org.scalatest.{FlatSpec, Matchers}
import io.github.mvillafuertem.advanced.Show64._

class Show64Test extends FlatSpec with Matchers {

  behavior of "Show 64"

  it should "encode" in {

    // G I V E N
    val device = Device(23L, "PEPE")

    // T H E N
    device.encode shouldBe "RGV2aWNlKDIzLFBFUEUp"
  }

}
