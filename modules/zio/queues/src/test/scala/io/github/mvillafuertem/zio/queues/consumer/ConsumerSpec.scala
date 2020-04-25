package io.github.mvillafuertem.zio.queues.consumer

import zio.test._
import zio.test.environment.TestEnvironment

object ConsumerSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)()

}
