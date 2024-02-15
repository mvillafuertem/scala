package io.github.mvillafuertem.cats.semaphore.itv

import cats.effect.IO
import cats.effect.std.{PQueue, Semaphore}
import cats.effect.unsafe.implicits.global
import cats.instances.list._
import cats.syntax.parallel._
import io.github.mvillafuertem.cats.semaphore.UnitTest

final class ITVTest extends UnitTest {

  "ITV" should {

    "WIP" ignore {

      val actual = for {
        queue     <- PQueue.unbounded[IO, Int]
        semaphore <- Semaphore[IO](1)
        itv        = ITV(queue, semaphore)
        _         <- (1 to 40).map(id => Vehiculo(id, itv).use).toList.parSequence
        _         <- (1 to 2).map(id => Puesto(id, itv).use).toList.parSequence
        size      <- queue.size
      } yield size

      actual.unsafeRunSync() shouldBe 0
    }

  }

}
