package io.github.mvillafuertem.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// --- Import just a module member, which is a function

@JSImport("./foo.js", "bar")
@js.native
object Member extends js.Function1[Int, Int] {
  def apply(i: Int): Int = js.native
}
