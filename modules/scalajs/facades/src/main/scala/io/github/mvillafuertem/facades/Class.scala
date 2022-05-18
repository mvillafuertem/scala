package io.github.mvillafuertem.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// --- Import a class

@JSImport("./class.js", JSImport.Namespace)
@js.native
class Class(val name: String, val age: Int) extends js.Object
