package io.github.mvillafuertem.tapir.infrastructure

import io.github.mvillafuertem.tapir.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.tapir.domain.model
import io.github.mvillafuertem.tapir.domain.model.{Product, ProductId, ProductType}
import io.github.mvillafuertem.tapir.infrastructure.SlickProductsRepositorySpec.SlickProductsRepositoryConfigurationSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import slick.basic.BasicBackend
import slick.jdbc.H2Profile.backend._
import zio.{BootstrapRuntime, UIO, ZIO}


final class SlickProductsRepositorySpec extends SlickProductsRepositoryConfigurationSpec with AnyFlatSpecLike
  with Matchers
  with BootstrapRuntime {

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

  it should "get all products from the database" in {

    // g i v e n
    val productId = ProductId()
    val name = "Product 1"
    val productType = ProductType.New
    val product: model.Product = Product(productId, name, productType)

    val productId2 = ProductId()
    val name2 = "Product 2"
    val productType2 = ProductType.Used
    val product2: model.Product = Product(productId2, name2, productType2)

    // w h e n
    this.unsafeRun(
      for {
        _ <- create(product)
        _ <- create(product2)
        result <- getAll
      } yield result
        .runCollect
        .map { result =>
          // t h e n
          result shouldBe Seq(product, product2)
        }
    )

  }

}

object SlickProductsRepositorySpec {

  trait SlickProductsRepositoryConfigurationSpec extends InfrastructureConfiguration
    with SlickProductsRepository {

    override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forConfig("infrastructure.h2"))

  }

}
