package io.github.mvillafuertem.zio

import io.github.mvillafuertem.zio.ZioApplication._
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}
import zio.test.environment.TestConsole

object ZioApplicationSpec extends DefaultRunnableSpec {
  def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    }
  )
}