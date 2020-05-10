package io.github.mvillafuertem.reflection

import org.scalatest.flatspec.AnyFlatSpecLike

final class ReflectionApplicationSpec extends AnyFlatSpecLike {


  /**
   * @see https://lampwww.epfl.ch/~michelou/scala/scala-reflection.html
   *      https://medium.com/@giposse/scala-reflection-d835832ed13a
   * OptManifest <— ClassManifest <— Manifest <— AnyValManifest
   *              \
   *               \— NoManifest
   */

  behavior of "Reflection Application"

  it should "Manifests give access to the Java reflection API (eg. method getMethods) without the need for instanciating a value of the instrumented type" in {

    def printMethods[T](t: T) { // requires instance
      val meths = t.getClass.getMethods
      println(meths take 5 mkString "\n")
    }
    def printMethods1(name: String) { // low-level
      val meths = Class.forName(name).getMethods
      println(meths take 5 mkString "\n")
    }
    def printMethods2[T: Manifest] { // no instance
      val meths = manifest[T].erasure.getMethods
      println(meths take 5 mkString "\n")
    }

    printMethods(Some(""))
    printMethods1("scala.Some")
    printMethods2[Some[_]]

  }

  it should "Subtyping relations can be tested using Scala manifests" in {

    def printSubtype[T, U](t: T, u: U) {
      val mt = Manifest.classType(t.getClass)
      val mu = Manifest.classType(u.getClass)
      println(mt <:< mu)
    }
    def printSubtype2[T: Manifest, U: Manifest] {
      println(manifest[T] <:< manifest[U])
    }
    // arrays are invariant
    printSubtype(Array(0), Array[AnyVal](1)) // false
    printSubtype(List(""), List[AnyRef](""))  // true
    printSubtype((Seq(0), 1), (Seq[AnyVal](), 2))  // true

    printSubtype2[Array[Int], Array[AnyVal]]
    printSubtype2[List[String], List[AnyRef]]
    printSubtype2[(Seq[Int], Int), (Seq[AnyVal], Int)]

  }

  it should "Finally manifests are necessary to create native Arrays whose element's class is not known at compile time." in {

    class Side(n: Int) {
      override def toString = "Side "+n
    }
    class Shape[T: Manifest](n: Int) {
      val sides = new Array[T](n)
      try {
        val ctr = manifest[T].erasure.getConstructor(classOf[Int])
        for (i <- 0 until n)
          sides(i) = ctr.newInstance(i:java.lang.Integer).asInstanceOf[T]
      }
      catch { case _ => }
    }
    val square = new Shape[Side](4)
    println(square.sides mkString "\n")

  }


}
