package io.github.mvillafuertem.advanced.typeclasses

import io.github.mvillafuertem.advanced.typeclasses.Show64._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Show64Test extends AnyFlatSpec with Matchers {

  behavior of "Show 64"

  it should "encode by implicit class" in {

    // G I V E N
    val device = Device(23L, "PEPE")

    // T H E N
    device.encode shouldBe "RGV2aWNlKDIzLFBFUEUp"

  }

  it should "encode by explicit method" in {

    // G I V E N
    val device = Device(23L, "PEPE")

    // T H E N
    encode(device)(Show64.deviceInterpreteShow64) shouldBe "RGV2aWNlKDIzLFBFUEUp"

  }

}
