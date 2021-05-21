package io.github.mvillafuertem.advanced.pattern.taglessfinal.typed

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object InterpreterAsFutureTypedBasic {

  val interpreterAsFuture: LanguageTypedBasic[Future] = new LanguageTypedBasic[Future] {
    override def build[T](v: T): Future[T] = Future(v)
  }

}
