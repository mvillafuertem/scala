package io.github.mvillafuertem.products.domain.repository

import io.github.mvillafuertem.products.domain.error.ProductException
import io.github.mvillafuertem.products.domain.model.{Product, ProductId}
import zio.IO

trait ProductsRepository {

  def create(product: Product): IO[ProductException, ProductId]

  def getAll: IO[ProductException, Seq[Product]]

}
