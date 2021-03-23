package io.github.mvillafuertem.advanced.typeclasses

import java.util.Base64

/**
 * @tparam A is the type
 */
trait Show64[A] {

  def encode(a: A): String

}

object Show64 {

  /**
   * @see io.github.mvillafuertem.advanced.pattern.taglessfinal
   *      Simplemente con esto podemos instanciar la interface
   *      sin hacer uso de `new`, es muy util si lo juntamos con
   *      implicitos, mirar los test
   */
  //def apply[A](implicit sh: Show64[A]): Show64[A] = sh
  //
  // this way [A: Show64] is called "context abstraction", means
  // that the compiler needs to find a semigroup of T in scope
  def apply[A: Show64]: Show64[A] = implicitly[Show64[A]]

  implicit class ShowOps[A: Show64](a: A) {
    def encode: String = Show64[A].encode(a)
  }

  implicit val deviceInterpreteShow64: Show64[Device] =
    (a: Device) => new String(Base64.getEncoder.encode(a.toString.getBytes))

  //def encode[A](a: A)(interprete: Show64[A]): String = interprete.encode(a)
  def encode[A: Show64](a: A): String = implicitly[Show64[A]].encode(a)

}
