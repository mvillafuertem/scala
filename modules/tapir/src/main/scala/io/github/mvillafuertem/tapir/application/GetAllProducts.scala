package io.github.mvillafuertem.tapir.application

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import zio.ZIO
import zio.stream.ZStream

/**
 * @author
 *   Miguel Villafuerte
 */
final class GetAllProducts {

  def apply(): ZIO[ProductsRepository, ProductException, ZStream[Any, Throwable, model.Product]] =
    ZIO.accessM[ProductsRepository](_.getAll)

}
