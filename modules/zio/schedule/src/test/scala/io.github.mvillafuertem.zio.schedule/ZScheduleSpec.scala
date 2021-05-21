package io.github.mvillafuertem.zio.schedule

import zio.Schedule
import zio.console.putStr
import zio.duration._
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestConsole, TestEnvironment }

object ZScheduleSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      testM("track a message")(
        assertM(
          for {
            _                  <- putStr("Start")
            fiberInitialDelay  <- putStr("Initial Delay")
                                    .delay(5.seconds) // Delay the print message
                                    .fork  // Fork is only necessary for testing
            _                  <- TestClock.adjust(5.seconds)
            fiberRepeatedDelay <- putStr("Repeated Delay")
                                    .repeat(
                                      Schedule.spaced(1.second) >>>
                                        Schedule.recurWhile(_ < 2)
                                    ) // Print the message every 1 second until twice
                                    .fork // Fork is only necessary for testing
            _                  <- TestClock.adjust(2.seconds)
            _                  <- fiberInitialDelay.join // This is not necessary
            _                  <- fiberRepeatedDelay.join // This is not necessary
            actual             <- TestConsole.output
          } yield actual
        )(equalTo(Vector("Start", "Initial Delay", "Repeated Delay", "Repeated Delay", "Repeated Delay")))
      ),
      testM("repeat a message using Duration")(
        assertM(
          for {
            fiberRepeatedMessage <- putStr("repeated message")
                                      .repeat(
                                        Schedule
                                          .exponential(10.milliseconds)
                                          .tapOutput(d => putStr(d.toString)) >>>
                                          Schedule
                                            .recurWhile(_ < 20.milliseconds)
                                      )
                                      .fork
            _                    <- TestClock.adjust(2.seconds)
            _                    <- fiberRepeatedMessage.join
            actual               <- TestConsole.output
          } yield actual
        )(equalTo(Vector("repeated message", "PT0.01S", "repeated message", "PT0.02S")))
      ),
      testM("repeat a message using recurs") {
        assertM(
          for {
            _      <- putStr("any stuff")
                        .repeat(
                          Schedule.recurs(6) >>>
                            Schedule.recurWhile(_ < 3) // Print the message N times until 3
                        )
            actual <- TestConsole.output
          } yield actual
        )(equalTo(Vector("any stuff", "any stuff", "any stuff", "any stuff")))
      }
//      testM("Schedule.unwrap should return a Schedule out of an effect")(
//        assertM(
//          for {
//            effect <- ZIO.succeed(5).map(Schedule.recurs)
//          } yield Schedule.unwrap(effect)
//        )(equalTo())
//      )
    ) //@@ zio.test.TestAspect.ignore
}
