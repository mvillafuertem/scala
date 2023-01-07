package io.github.mvillafuertem.tapir.application

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import zio.IO
import zio.stream.ZStream

/**
 * @author
 *   Miguel Villafuerte
 */
final class GetAllProducts {

  // type projection in scala3 https://docs.scala-lang.org/scala3/reference/dropped-features/type-projection.html
  // type T[A] = IO[ProductException, A]
  // ZStream[ProductsRepository[T], ProductException, model.Product]
  def apply(): ZStream[ProductsRepository[({ type T[A] = IO[ProductException, A] })#T], ProductException, model.Product] =
    ZStream.environmentWithStream[ProductsRepository[({ type T[A] = IO[ProductException, A] })#T]](_.get.getAll)

}
