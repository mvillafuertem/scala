package io.github.mvillafuertem.advanced.typeclasses

trait Show64[A] {

  def encode(a: A): String

}
