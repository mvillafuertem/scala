package io.github.mvillafuertem.tapir.domain.repository

import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductId }
import zio.IO
import zio.stream.ZStream

trait ProductsRepository {

  def create(product: Product): IO[ProductException, ProductId]

  def getAll: IO[ProductException, ZStream[Any, Throwable, Product]]

  def find: IO[Unit, String]

}
