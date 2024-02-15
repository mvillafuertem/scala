package io.github.mvillafuertem.cats.semaphore.itv

import cats.Monad
import cats.effect.IO
import cats.syntax.show._

final case class Puesto(identif: Int, itv: ITV) {

  def use: IO[Unit] =
    itv.queue.size.flatMap(size => IO.println(show"cars waiting $size")) >>
      Monad[IO].whileM_(itv.queue.size.map(_ > 0))(
        itv.removeCar().flatMap(id => IO.println(show"El puesto $identif ha revisado el coche $id"))
      ) >>
      IO.println(show"Fin del puesto $identif")

}
