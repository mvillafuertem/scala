package io.github.mvillafuertem.advanced.`type`.classes

import io.github.mvillafuertem.advanced.`type`.classes.Device
import io.github.mvillafuertem.advanced.`type`.classes.Show64._
import org.scalatest.{FlatSpec, Matchers}

class Show64Test extends FlatSpec with Matchers {

  behavior of "Show 64"

  it should "encode" in {

    // G I V E N
    val device = Device(23L, "PEPE")

    // T H E N
    device.encode shouldBe "RGV2aWNlKDIzLFBFUEUp"

  }

}
