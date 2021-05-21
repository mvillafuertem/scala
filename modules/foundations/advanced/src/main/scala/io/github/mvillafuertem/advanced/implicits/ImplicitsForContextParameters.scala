package io.github.mvillafuertem.advanced.implicits

import scala.concurrent.{ ExecutionContext, Future }

object ImplicitsForContextParameters {

  /**
   *
   * Implicits are not available
   */
  final def fooWithout(a: Int)(ec: ExecutionContext): Future[Boolean] =
    barWithout(a)(ec)
      .map(_.length > 0)(ec)

  /**
   *
   * Implicits are not available
   */
  final def barWithout(a: Int)(ec: ExecutionContext): Future[String] =
    Future(a.toString)(ec)

  /**
   * Ver la diferencia cuando los implicitos no están disponibles,
   * pero estamos acoplando la lógica de negocio con las necesidades de ejecución.
   * Para que esto no pasa, en Dotty tenemos implicit functions types o podemos usar la Reader Monad
   */
  final def fooWith(a: Int)(implicit ec: ExecutionContext): Future[Boolean] =
    barWith(a).map(_.length > 0)

  final def barWith(a: Int)(implicit ec: ExecutionContext): Future[String] =
    Future(a.toString)

}
