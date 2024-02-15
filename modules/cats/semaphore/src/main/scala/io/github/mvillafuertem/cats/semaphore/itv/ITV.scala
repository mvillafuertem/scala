package io.github.mvillafuertem.cats.semaphore.itv

import cats.effect.IO
import cats.effect.std.{PQueue, Queue, Semaphore}

final case class ITV(queue: PQueue[IO, Int], semaphore: Semaphore[IO]) {

  def addCar(id: Int): IO[Unit] =
    for {
      _ <- semaphore.acquire
      _ <- queue.offer(id)
      _ <- semaphore.release
    } yield ()

  def removeCar(): IO[Int] =
    queue.size
      .map(_ > 0)
      .ifM(
        for {
          _  <- IO.println("Removing car")
          _  <- semaphore.acquire
          id <- queue.take
          _  <- semaphore.release
          _  <- IO.println("Release car")
        } yield id,
        IO.pure(-1)
      )

}

/*
class ITV {

  import java.util.PriorityQueue
  import java.util.concurrent.Semaphore
  private var semaforo: Semaphore                 = new Semaphore(1)
  private var listaCoches: PriorityQueue[Integer] = new PriorityQueue[Integer]
  private var tiempoTotal: Integer                = 0

  def nuevoCoche(numeroCoche: Integer): Unit = {
    try {
      semaforo.acquire()
      listaCoches.add(numeroCoche)
      semaforo.release()
    } catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
  }

  def terminarCoche(tiempoParcial: Integer): Int = {
    var coche = 0
    try
      if (isCochesPendientes) {
        semaforo.acquire()
        coche = listaCoches.poll
        tiempoTotal += tiempoParcial
        semaforo.release()
      }
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
    coche
  }

  def isCochesPendientes: Boolean = listaCoches.size > 0

  def getTiempoTotal: Integer = tiempoTotal
}

class Puesto(private var identif: Int, private var itv: ITV) extends Thread {
  this.tiempoPuesto = 0
  private var tiempoPuesto: Integer = null

  override def run(): Unit = {
    var retardo     = 0
    var numeroCoche = 0
    while (itv.isCochesPendientes) try {
      retardo = (Math.random * 90 + 10).toInt
      tiempoPuesto += retardo
      numeroCoche = itv.terminarCoche(retardo)
      Thread.sleep(retardo)
      System.out.println(
        "El puesto " + identif + " ha revisado el coche " + numeroCoche + " en un tiempo de " + retardo
      )
    } catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
    System.out.println("Fin del puesto " + identif + ", que termina con un tiempo parcial de " + tiempoPuesto)
  }
}

class Vehiculo(private var identif: Int, private var itv: ITV) extends Thread {
  override def run(): Unit = {
    itv.nuevoCoche(identif)
  }
}

object Principal {
  def main(args: Array[String]): Unit = {
    val pueRandom = (Math.random * 4).toInt + 1
    val vehRandom = (Math.random * 30).toInt + 20
    val itv       = new ITV
    System.out.println(vehRandom + " Vehículos serán atendidos por " + pueRandom + " puestos.")
    // Creación de vehículos
    val v = new Array[Vehiculo](vehRandom)
    for (i <- 0 until vehRandom) {
      v(i) = new Vehiculo(i + 1, itv)
      v(i).start()
    }
    // Creación de puestos
    val p = new Array[Puesto](pueRandom)
    for (i <- 0 until pueRandom) {
      p(i) = new Puesto(i + 1, itv)
      p(i).start()
    }
    // Se espera a que terminen todos los puestos
    for (i <- 0 until pueRandom) {
      try p(i).join()
      catch {
        case e: InterruptedException =>
          e.printStackTrace()
      }
    }
    // Se espera a que terminen todos vehículos
    for (i <- 0 until vehRandom) {
      try v(i).join()
      catch {
        case e: InterruptedException =>
          // TODO Auto-generated catch block
          e.printStackTrace()
      }
    }
    // Se cierra la itv
    System.out.println("Se cierra la ITV con un tiempo acumulado de " + itv.getTiempoTotal)
  }
}
*/