package io.github.mvillafuertem.advanced.implicits

object ImplicitsForExtensionMethods extends App {

  class MyStringOpsAdapter(s: String) {

    final def mapToDummy(f: Char => Char): String = "dummy"

  }

  implicit def toMyStringOpsAdapter(s: String): MyStringOpsAdapter = new MyStringOpsAdapter(s)

}
