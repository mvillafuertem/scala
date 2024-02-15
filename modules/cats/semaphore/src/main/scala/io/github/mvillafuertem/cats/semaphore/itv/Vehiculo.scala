package io.github.mvillafuertem.cats.semaphore.itv

import cats.effect.IO
import cats.syntax.show._

final case class Vehiculo(identif: Int, itv: ITV) {

  def use: IO[Unit] =
    IO.println(show"Adding car $identif") >> itv.addCar(identif)

}
