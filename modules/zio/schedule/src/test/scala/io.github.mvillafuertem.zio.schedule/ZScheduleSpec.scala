package io.github.mvillafuertem.zio.schedule

import zio.console.putStrLn
import zio.duration._
import zio.test.Assertion.{ equalTo, isUnit }
import zio.test._
import zio.test.environment.{ TestClock, TestEnvironment }
import zio.{ Schedule, Task, UIO, ZIO }

object ZScheduleSpec extends DefaultRunnableSpec {

  val policy1: Schedule[Any, Any, Duration] =
    Schedule.exponential(10.milliseconds).tapOutput(o => UIO(println(o))) >>>
      Schedule.recurWhile(_ < 2.seconds)

  val policy2: Schedule[Any, Any, (Long, Long)] =
    Schedule.recurs(5) ||
      Schedule.recurs(10)

  override def spec: ZSpec[TestEnvironment, Any] =
    suite(getClass.getSimpleName)(
      testM("track a message")(
        assertM(
          for {
            _ <- putStrLn("Start")
            _ <- putStrLn("Initial Delay").delay(5.seconds).fork
            _ <- TestClock.adjust(5.seconds)
            _ <- putStrLn("Repeated Delay").repeat(Schedule.spaced(1.second))
          } yield ()
        )(isUnit)
      ),
      testM("repeat a message")(
        assertM(
          for {
            _ <- putStrLn("repeated message")
                   .repeat(policy1)
                   .fork
            _ <- TestClock.adjust(2.seconds)
          } yield ()
        )(isUnit)
      )
//      testM("Schedule.unwrap should return a Schedule out of an effect")(
//        assertM(
//          for {
//            effect <- ZIO.succeed(5).map(Schedule.recurs)
//          } yield Schedule.unwrap(effect)
//        )(equalTo())
//      )
    ) @@ zio.test.TestAspect.ignore
}
