package io.github.mvillafuertem.cats.semaphore

import cats.effect.std.Semaphore
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Ref}
import cats.syntax.parallel._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class OrdenTest extends UnitTest {

  "Orden" should {

    "wip" in {
      // g i v e n

      // w h e n
      val actual = for {
        semaphore <- Semaphore[IO](0)
        sequence  <- Ref.of[IO, Seq[Char]](Seq.empty[Char])
        orden1     = new Orden("1111", 'a', sequence, semaphore)
        orden2     = new Orden("2222", '0', sequence, semaphore)
        _         <- Seq(orden1.use, orden2.use).parSequence.void
        actual    <- sequence.get
      } yield actual

      // t h e n
      actual.unsafeRunSync() shouldBe List('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')
    }

  }

}
