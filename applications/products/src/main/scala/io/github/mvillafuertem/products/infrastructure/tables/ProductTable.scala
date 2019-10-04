package io.github.mvillafuertem.products.infrastructure.tables

import java.util.UUID

import io.github.mvillafuertem.products.domain.model
import io.github.mvillafuertem.products.domain.model.{Product, ProductId, ProductType}
import slick.driver.H2Driver.api._
import slick.lifted.CaseClassShape

trait ProductTable {

  implicit object ProductShape extends CaseClassShape(LiftedProduct.tupled, Product.tupled)

  implicit def productIdMapper: BaseColumnType[ProductId] = MappedColumnType.base[ProductId, UUID](
    vo => vo.id,
    dbo => model.ProductId(dbo)
  )

  implicit def productTypeMapper: BaseColumnType[ProductType] = MappedColumnType.base[ProductType, String](
    vo => vo.toString.toLowerCase,
    dbo => model.ProductType.find(dbo)
  )

  case class LiftedProduct(productId: Rep[ProductId], name: Rep[String], productType: Rep[ProductType])

  final class Products(tag: Tag) extends Table[Product](tag, "ASSETS") {
    // P R I M A R Y  K E Y
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def productId = column[ProductId]("PRODUCT_ID", O.PrimaryKey)

    // C O L U M N S
    def name = column[String]("NAME")

    def productType = column[ProductType]("PRODUCT_TYPE")

    // P R O J E C T I O N
    def * = LiftedProduct(productId, name, productType)
  }

}

object ProductTable extends ProductTable
