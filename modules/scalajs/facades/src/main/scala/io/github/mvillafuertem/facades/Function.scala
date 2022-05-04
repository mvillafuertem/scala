package io.github.mvillafuertem.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// --- Import a whole module as a function

@JSImport("./function.js", JSImport.Default)
@js.native
object Function extends js.Function2[Int, Int, String] {
  def apply(first: Int, second: Int): String = js.native
}
