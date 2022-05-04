package io.github.mvillafuertem

import io.github.mvillafuertem.facades.{ Foo, Member }

object FacadesApp {

  def main(args: Array[String]): Unit = {
    println(Foo.bar(42))
    println(Member(42))
    val user = new facades.Class("Julien", 30)
    println(user.name)
    println(facades.Function(8, 12))
  }

}
