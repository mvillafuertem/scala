package io.github.mvillafuertem.products.api

import akka.http.scaladsl.server.Route
import io.github.mvillafuertem.products.api.TapirAkkaHttpServerWithZIO._
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
/**
  * @author Miguel Villafuerte
  */
final class ProductsApi(productsRepository: ProductsRepository) extends ProductsEndpoint {

  val route: Route = productsEndpoint.toRoute(_ => productsRepository.find)

}
