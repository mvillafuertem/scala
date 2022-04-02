package io.github.mvillafuertem.tapir.domain.repository

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductId }
import zio.{ stream, IO }

trait ProductsRepository {

  def create(product: Product): IO[ProductException, ProductId]

  def getAll: stream.Stream[ProductException, Product]

  def find: IO[Unit, String]

}
