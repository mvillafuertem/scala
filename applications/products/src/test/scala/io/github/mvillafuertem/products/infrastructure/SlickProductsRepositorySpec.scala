package io.github.mvillafuertem.products.infrastructure

import io.github.mvillafuertem.products.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.products.domain.model
import io.github.mvillafuertem.products.domain.model.{Product, ProductId, ProductType}
import io.github.mvillafuertem.products.infrastructure.SlickProductsRepositorySpec.SlickProductsRepositoryConfigurationSpec
import org.scalatest.{FlatSpecLike, Matchers}
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.{DefaultRuntime, UIO, ZIO}


final class SlickProductsRepositorySpec extends SlickProductsRepositoryConfigurationSpec with FlatSpecLike
  with Matchers
  with DefaultRuntime {

  behavior of "SlickProductsRepositorySpec"

  it should "insert an product into the database" in {

    // g i v e n
    val productId = ProductId()
    val name = "Product 1"
    val productType = ProductType.New
    val product: model.Product = Product(productId, name, productType)

    // w h e n
    val actual: ProductId = this.unsafeRun(create(product))

    // t h e n
    actual shouldBe productId

  }

}

object SlickProductsRepositorySpec {

  trait SlickProductsRepositoryConfigurationSpec extends InfrastructureConfiguration
    with SlickProductsRepository {

    override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forConfig("infrastructure.h2"))

  }

}
