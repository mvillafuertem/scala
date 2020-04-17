package io.github.mvillafuertem.tapir.api

import akka.http.scaladsl.server.Route
import io.github.mvillafuertem.tapir.api.TapirAkkaHttpServerWithZIO._
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
/**
  * @author Miguel Villafuerte
  */
final class ProductsApi(productsRepository: ProductsRepository) extends ProductsEndpoint {

  val route: Route = productsEndpoint.toRoute(_ => productsRepository.find)

}
