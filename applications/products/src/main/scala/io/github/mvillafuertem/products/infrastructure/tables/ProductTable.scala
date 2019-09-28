package io.github.mvillafuertem.products.infrastructure.tables

import java.util.UUID

import io.github.mvillafuertem.products.domain.{ProductId, model}
import io.github.mvillafuertem.products.domain.model.{Product, ProductId}
import slick.lifted.CaseClassShape
import slick.driver.H2Driver.api._

trait ProductTable {

  implicit object ProductShape extends CaseClassShape(LiftedProduct.tupled, Product.tupled)

  implicit def productIdIdMapper: BaseColumnType[ProductId] = MappedColumnType.base[ProductId, UUID](
    vo => vo.id,
    dbo => model.ProductId(dbo)
  )

  case class LiftedProduct(productId: Rep[ProductId], name: Rep[String])

  final class Products(tag: Tag) extends Table[Product](tag, "ASSETS") {
    // P R I M A R Y  K E Y
    def id = column[ProductId]("PRODUCT_ID", O.PrimaryKey)

    // C O L U M N S
    def name = column[String]("NAME")

    // P R O J E C T I O N
    def * = LiftedProduct(id, name)
  }

}

object ProductTable extends ProductTable
