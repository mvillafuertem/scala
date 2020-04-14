package io.github.mvillafuertem.products.application

import io.github.mvillafuertem.products.domain.error.ProductException
import io.github.mvillafuertem.products.domain.model
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
import zio.ZIO
import zio.stream.ZStream

/**
 * @author Miguel Villafuerte
 */
final class GetAllProducts {

  def apply(): ZIO[ProductsRepository, ProductException, ZStream[Any, Throwable, model.Product]] =
    ZIO.accessM[ProductsRepository](_.getAll)

}
