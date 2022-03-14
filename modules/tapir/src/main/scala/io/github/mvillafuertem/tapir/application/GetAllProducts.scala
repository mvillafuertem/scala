package io.github.mvillafuertem.tapir.application

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import zio.stream.ZStream

/**
 * @author
 *   Miguel Villafuerte
 */
final class GetAllProducts {

  def apply(): ZStream[ProductsRepository, ProductException, model.Product] =
    ZStream.environmentWithStream[ProductsRepository](_.get.getAll)

}
