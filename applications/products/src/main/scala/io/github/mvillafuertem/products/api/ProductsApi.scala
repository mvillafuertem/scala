package io.github.mvillafuertem.products.api

import io.github.mvillafuertem.products.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
/**
  * @author Miguel Villafuerte
  */
final class ProductsApi(productsRepository: ProductsRepository,
                        infrastructureConfiguration: InfrastructureConfiguration) extends ProductsEndpoint {



//  productsEndpoint.toRoute(
//    query => productsRepository.getAll.provide(infrastructureConfiguration)
//  )



}
