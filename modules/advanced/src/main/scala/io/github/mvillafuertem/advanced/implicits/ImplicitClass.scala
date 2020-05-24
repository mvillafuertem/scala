package io.github.mvillafuertem.advanced.implicits

/**
 * Su funcionamiento es el mismo que ImplicitForExtensionMethods
 * pero te quitas el boilerplate de crear metodos implicitos a partir
 * de una clase
 */
object ImplicitClass {


  implicit class MyStringOpsAdapter(s: String) {

    final def mapToDummy(f: Char => Char): String = "dummy"

  }

}
