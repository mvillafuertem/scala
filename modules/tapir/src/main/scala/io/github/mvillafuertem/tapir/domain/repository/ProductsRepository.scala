package io.github.mvillafuertem.tapir.domain.repository

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductId }
import zio.stream

trait ProductsRepository[F[_]] {

  def create(product: Product): F[ProductId]

  def getAll: stream.Stream[ProductException, Product]

  def find: F[String]

}
