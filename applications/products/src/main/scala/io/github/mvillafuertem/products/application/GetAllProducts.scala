package io.github.mvillafuertem.products.application

import io.github.mvillafuertem.products.domain.error.ProductException
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
import zio.ZIO

/**
 * @author Miguel Villafuerte
 */
final class GetAllProducts {

  def apply(): ZIO[ProductsRepository, ProductException, Seq[Product]] =
    ZIO.accessM[ProductsRepository](_.getAll)

}
