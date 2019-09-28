package io.github.mvillafuertem.products.domain.repository

import io.github.mvillafuertem.products.domain.error.ProductException
import io.github.mvillafuertem.products.domain.model.ProductId
import zio.IO

trait ProductsRepository {

  def create(name: String): IO[ProductException, ProductId]

}
