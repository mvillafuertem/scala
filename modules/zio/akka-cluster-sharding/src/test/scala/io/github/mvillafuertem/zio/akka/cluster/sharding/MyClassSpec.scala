package io.github.mvillafuertem.zio.akka.cluster.sharding

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import zio.console.Console
import zio.stream.ZStream
import zio.{ BootstrapRuntime, Schedule, Task, ZIO }

final class MyClassSpec extends AnyFlatSpecLike with BootstrapRuntime with Matchers {

  "MyClass" should "Stream" in {

    val clazz = new MyClass

    val value = unsafeRun(clazz.a.runCollect)

    value should have size 10

  }

  it should "Stream pull" in {

    val value: Int = 1

    val actual = unsafeRun(
      ZStream
        .fromEffect(
          Task
            .effect(value)
            .mapError(Some(_))
            .flatMap { a =>
              if (a == 1) ZIO.fail(None)
              else ZIO.succeed(a)

            }
        )
        .runCollect
    )

    actual shouldBe List()

  }

  it should "Stream repeat effect with" in {

//    val value: Int = 1
//
//    val actual = unsafeRun(ZStream.repeatEffectWith(
//      Task.effect(value), Schedule.spaced(5.seconds)
//    ).tap(a => UIO(println(a)))
//      .runCollect
//    )
//
//    actual shouldBe List()

  }

  it should "Stream combine" in {

    val clazz = new MyClass

    val program: ZIO[Console, Nothing, Seq[Int]] =
      (for {
        _      <- zio.console.putStrLn("Hola")
        stream <- clazz.a.runCollect
        _      <- zio.console.putStrLn("Adios")
      } yield stream).orDie

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
import io.github.mvillafuertem.zio.akka.cluster.sharding.MyClass._

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

      _      <- zio.console.putStrLn("Hola")
      stream <- my.a.runCollect
      _      <- zio.console.putStrLn("Adios")

    } yield stream) repeat foo.tapOutput(a => zio.ZIO.debug(s"Completed $a"))

}
