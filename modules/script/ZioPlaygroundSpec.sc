import $ivy.`dev.zio::zio-test-sbt:1.0.10`
import $ivy.`dev.zio::zio-test:1.0.10`
import zio.blocking.{ effectBlocking, Blocking }
import zio.console.{ putStrLn, Console }
import zio.duration._
import zio.internal.Executor
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.{ TestClock, TestEnvironment }
import zio.{ Chunk, RIO, Schedule, ZIO, ZLayer }

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

// amm `pwd`/modules/script/ZioPlaygroundSpec.sc
// https://github.com/zio/zio/issues/5173
// https://github.com/zio/zio/issues/3840
ZioPlaygroundSpec.main(Array())

object ZioPlaygroundSpec extends DefaultRunnableSpec {

  def sideEffect: ZIO[Console with Blocking, Throwable, Boolean] =
    for {
      _      <- putStrLn("START")
      allFib <- zio.blocking.effectBlocking(Thread sleep 1000L).fork
      r      <- ZIO.effectAsync[Any, Throwable, Boolean](
        cb =>
          Future { Thread.sleep(1000L); true } onComplete {
            case scala.util.Success(value)     => cb(ZIO.effect(value))
            case scala.util.Failure(exception) => cb(ZIO.fail(exception))
          },
        List(allFib.id)
      )
      _      <- putStrLn("STOP")
    } yield r

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite(getClass.getSimpleName)(
      testM(s"repeat policy")(
        assertM(
          for {
            fiber  <- sideEffect
              .repeat(
                (Schedule.spaced(2.second) >>> Schedule.recurWhile[Long](_ < 5)) *>
                  Schedule.collectAll[Boolean].tapInput[Console, Boolean](response => putStrLn(response.toString).exitCode)
              )
              .catchAll(_ => RIO.effect(Chunk(false)))
              .fork
            _      <- TestClock.adjust(20.seconds)
            _      <- putStrLn("Adjusted")
            actual <- fiber.join
            _      <- putStrLn("Joined")
          } yield actual
        )(equalTo(Chunk.fill(6)(true)))
      )
    ) @@ TestAspect.timed
}
