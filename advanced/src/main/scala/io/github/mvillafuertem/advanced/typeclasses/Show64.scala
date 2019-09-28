package io.github.mvillafuertem.advanced.typeclasses

import java.util.Base64

trait Show64[A] {

  def encode(a: A): String

}

object Show64 {

  def apply[A](implicit sh: Show64[A]): Show64[A] = sh

  def encode[A: Show64](a: A): String = Show64[A].encode(a)

  implicit val deviceShow64: Show64[Device] =
    (a: Device) => new String(Base64.getEncoder.encode(a.toString.getBytes))

  implicit class ShowOps[A: Show64](a: A) {
    def encode: String = Show64[A].encode(a)
  }
}