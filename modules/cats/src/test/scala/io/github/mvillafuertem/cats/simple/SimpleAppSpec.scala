package io.github.mvillafuertem.cats.simple

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpecLike

final class SimpleAppSpec extends AnyFlatSpecLike {

  behavior of s"${getClass.getSimpleName}"

  it should "WIP" in {


    IO("Hola").unsafeRunSync()

    val unit = SimpleApp
      .greatUser[IO]("Hola")
      .unsafeRunSync()

    println("Hola")

  }

}
