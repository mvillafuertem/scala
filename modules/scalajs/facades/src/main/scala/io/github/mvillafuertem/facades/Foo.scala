package io.github.mvillafuertem.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// --- Import a whole module as an object

// FIXME: this is a workaround
@JSImport("../../../src/main/resources/foo.js", JSImport.Namespace)
@js.native
object Foo extends js.Object {
  def bar(i: Int): Int = js.native
}
