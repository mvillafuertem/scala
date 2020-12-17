package io.github.mvillafuertem.tapir.api

import akka.http.scaladsl.server.Route
import io.github.mvillafuertem.tapir.ProductsServiceApplication.unsafeRunToFuture
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import sttp.tapir.server.akkahttp._

/**
 * @author Miguel Villafuerte
 */
final class ProductsApi(productsRepository: ProductsRepository) extends ProductsEndpoint {

  val route: Route = productsEndpoint.toRoute(_ => unsafeRunToFuture(productsRepository.find.either).future)

}
