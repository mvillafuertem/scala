package io.github.mvillafuertem.products.infrastructure

import io.github.mvillafuertem.products.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.products.domain.model
import io.github.mvillafuertem.products.domain.model.{Product, ProductId, ProductType}
import io.github.mvillafuertem.products.infrastructure.SlickProductsRepositorySpec.SlickProductsRepositoryConfigurationSpec
import org.scalatest.{FlatSpecLike, Matchers}
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.stream.ZStream
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

  ignore should "get all products from the database" in {

    // g i v e n
    val productId = ProductId()
    val name = "Product 1"
    val productType = ProductType.New
    val product: model.Product = Product(productId, name, productType)

    val productId2 = ProductId()
    val name2 = "Product 2"
    val productType2 = ProductType.New
    val product2: model.Product = Product(productId2, name2, productType2)

    // w h e n
    val actual: ZStream[Any, Throwable, Product] = this.unsafeRun(
      for {
        _ <- create(product)
        _ <- create(product2)
        result <- getAll
      } yield result
    )

    // t h e n
    actual shouldBe "productId"

  }

}

object SlickProductsRepositorySpec {

  trait SlickProductsRepositoryConfigurationSpec extends InfrastructureConfiguration
    with SlickProductsRepository {

    override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forConfig("infrastructure.h2"))

  }

}
