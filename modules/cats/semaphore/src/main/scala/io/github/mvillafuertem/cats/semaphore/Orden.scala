package io.github.mvillafuertem.cats.semaphore

import cats.effect.std.{Console, Semaphore}
import cats.effect.{IO, IOApp, Ref}
import cats.syntax.all._
import cats.{Applicative, Monad}

final case class Orden[F[_]: Monad: Console](id: String, c: Char, sequence: Ref[F, Seq[Char]], semaphore: Semaphore[F]) {

  def use: F[Unit] = for {
    x <- semaphore.available
    _ <- Console[F].println(s"Hilo $id --- Available ${x}")
    _ <- if (c == 'a') Console[F].println(s"Hilo $id --- Acquire ${x}") >> semaphore.acquire else Applicative[F].pure(())
    _ <- sequence.updateAndGet{ref =>
      val updatedSeq = (0 until 10).foldLeft(ref) { (accSeq, i) =>
        System.out.println(s"Hilo $id --- " + c + i + " " + (c + i).toChar)
        accSeq :+ (c + i).toChar
      }
      updatedSeq
    }
    _ <- if (c == '0') Console[F].println(s"Hilo $id --- Release ${x}") >> semaphore.release else Applicative[F].pure(())
    z <- semaphore.available
    _ <- Console[F].println(s"Hilo $id --- Available ${z}")
  } yield ()
}

object Orden extends IOApp.Simple {

  override def run: IO[Unit] = for {

    semaphore <- Semaphore[IO](0)
    sequence    <- Ref.of[IO, Seq[Char]](Seq.empty[Char])
    orden1     = new Orden("1111", 'a', sequence, semaphore)
    orden2     = new Orden("2222", '0', sequence, semaphore)
    _         <- Seq(orden1.use, orden2.use).parSequence.void
  } yield ()

}

/*
object Orden {
  import java.util.concurrent.Semaphore

  def main(args: Array[String]): Unit = {
    val semaphore = new Semaphore(0)
    val t1        = new Orden("1111", 'a', semaphore)
    val t2        = new Orden("2222", '0', semaphore)
    t1.start()
    t2.start()
    try {
      t1.join()
      t2.join()
    } catch {
      case e: InterruptedException =>
        System.out.println("Hilo principal del proceso interrumpido.")
    }
    System.out.println("Proceso terminado.")
  }
}

class Orden(id: String, c: Char, sem: java.util.concurrent.Semaphore) extends Thread {
  override def run(): Unit = {
    if (c == '0')
      try sem.acquire
      catch {
        case e: InterruptedException =>
          e.printStackTrace()
      }
    for (i <- 0 until 50) {
      System.out.println(s"Hilo $id --- " + c + i + " " + (c + i).toChar)
    }
    if (c == 'a') sem.release
  }
}
 */



/*def apply[F[_], S <: STM[F]](db: S#TVar[List[Car]]): CarRepository[S#Txn] =
  new CarRepository[S#Txn] {
    override def findByAvailableSeats(availableSeats: Int): S#Txn[List[Car]] = db.get

    override def deleteAll(): S#Txn[Unit] = db.modify(_ => List.empty)

    override def deleteById(id: Int): S#Txn[Unit] = db.modify(_.filterNot(_.id.value == id))

    override def findById(id: Int): S#Txn[Option[Car]] = db.get.map(_.find(_.id.value == id))

    override def getAll: S#Txn[List[Car]] = db.get

    override def insert(value: Car): S#Txn[Car] = db.modify(_ :+ value).as(value)

    override def insertAll(value: Car*): S#Txn[List[Car]] = ???

    override def update(a: Car): S#Txn[Car] = db.modify(_.filterNot(_.id.value == a.id.value) :+ a).as(a)
  }

val actual: Seq[Car] = (for {

  stm <- STM.runtime[IO]
  db  <- stm.commit(stm.TVar.of(List(Car(CarId(1), Seat(4), Seat(4)))))
  repository = InMemoryCarRepository[IO, stm.type](db)
  txn <- stm
    .commit {
      for {
        _    <- repository.insert(Car(CarId(0), Seat(4), Seat(4)))
        _    <- stm.raiseError(new RuntimeException("error provocado"))
        cars <- repository.getAll
      } yield cars
    }
    .handleErrorWith(_ => stm.commit(repository.getAll))

} yield txn).unsafeRunSync()

actual shouldBe List(Car(CarId(1), Seat(4), Seat(4)))*/
