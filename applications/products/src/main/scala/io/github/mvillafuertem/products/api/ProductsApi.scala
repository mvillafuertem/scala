package io.github.mvillafuertem.products.api

import io.github.mvillafuertem.products.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
import io.github.mvillafuertem.products.infrastructure.SlickProductsRepository
import tapir.server.akkahttp._
/**
  * @author Miguel Villafuerte
  */
final class ProductsApi(productsRepository: ProductsRepository,
                        infrastructureConfiguration: InfrastructureConfiguration) extends ProductsEndpoint {



//  productsEndpoint.toRoute(
//    query => productsRepository.getAll.provide(infrastructureConfiguration)
//  )



}
