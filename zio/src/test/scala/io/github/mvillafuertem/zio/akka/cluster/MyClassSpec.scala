package io.github.mvillafuertem.zio.akka.cluster

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import zio.console.Console
import zio.stream.ZStream
import zio.{DefaultRuntime, Schedule, ZIO}


final class MyClassSpec extends AnyFlatSpecLike with DefaultRuntime with Matchers {

  "MyClass" should "Stream" in {


    val clazz = new MyClass

    val value = unsafeRun(clazz.a.runCollect)

    value should have size 10


  }

  it should "Stream combine" in {


    val clazz = new MyClass

    val program: ZIO[Console, Nothing, Seq[Int]] = for {

      _ <- zio.console.putStrLn("Hola")
      stream <- clazz.a.runCollect
      _ <- zio.console.putStrLn("Adios")

    } yield stream

    val value: Seq[Int] = unsafeRun(program)
    value should have size 10


  }


  "Pepe" should "Stream" in {


    //val clazz = new MyClass
    //val pepe = new Pepe(clazz)

    //val value = unsafeRun(pepe.b)

    //value shouldBe 1


  }

}



// MY TRAIT
trait My[F[_]] {

  def a: F[_]
}


// MY CLASS
import io.github.mvillafuertem.zio.akka.cluster.MyClass._

class MyClass extends My[MyType] {
  override def a: ZStream[Any, Nothing, Int] = ZStream.range(1, 10)
}

object MyClass {

  type MyType[_] = ZStream[Any, Nothing, Int]

}


// action repeat policy
class Pepe(my: My[MyType]) {

  import zio.duration._
  val foo = Schedule.spaced(1 second)

  def b =
    (for {

      _ <- zio.console.putStrLn("Hola")
      stream <- my.a.runCollect
      _ <- zio.console.putStrLn("Adios")

    } yield stream) repeat foo.tapOutput(a => zio.console.putStrLn(s"Completed $a"))

}
