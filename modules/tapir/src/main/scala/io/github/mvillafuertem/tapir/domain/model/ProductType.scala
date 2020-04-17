package io.github.mvillafuertem.tapir.domain.model

/**
 * @author Miguel Villafuerte
 */
sealed trait ProductType

object ProductType {

  case object New extends ProductType
  case object Used extends ProductType

  val productTypes: Set[ProductType] = Set(New, Used)

  def find(productType: String): ProductType =
    productTypes.find(_.toString.equalsIgnoreCase(productType)) match {
      case Some(value) => value
      case None => throw new RuntimeException()
    }

}
