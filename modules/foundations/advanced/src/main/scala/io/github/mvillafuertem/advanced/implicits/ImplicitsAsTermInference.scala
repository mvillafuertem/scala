package io.github.mvillafuertem.advanced.implicits

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Se pueden entiende inferencia de terminos, igual que inferencia de tipos. Mirar test
 */
object ImplicitsAsTermInference {

  final def apply[T](body: => T)(implicit ec: ExecutionContext): Future[T] = Future(body)

}
