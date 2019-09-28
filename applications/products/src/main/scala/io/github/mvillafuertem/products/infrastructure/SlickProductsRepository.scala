package io.github.mvillafuertem.products.infrastructure

import slick.driver.H2Driver.api._
import io.github.mvillafuertem.products.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.products.domain.error.ProductException
import io.github.mvillafuertem.products.domain.model.{Product, ProductId}
import io.github.mvillafuertem.products.domain.repository.ProductsRepository
import io.github.mvillafuertem.products.infrastructure.tables.ProductTable
import slick.dbio.{DBIO, StreamingDBIO}
import zio.{IO, ZIO}
import zio.interop.reactiveStreams._
import zio.stream.ZStream

trait SlickProductsRepository extends ProductsRepository with InfrastructureConfiguration {
  self =>

  import SlickProductsRepository._
  import ProductTable._

  val products = TableQuery[ProductTable.Products]


  override def create(name: String): IO[ProductException, ProductId] = {

    val insert = (products returning products.map(_.id)) += Product(ProductId(), name)
    ZIO.fromDBIO(insert).provide(self).refineOrDie {
      case e: Exception => new ProductException(e)
    }

  }


}

object SlickProductsRepository {

  implicit class ZIOObjOps(private val obj: ZIO.type) extends AnyVal {
    def fromDBIO[R](dbio: DBIO[R]): ZIO[InfrastructureConfiguration, Throwable, R] =
      for {
        db <- ZIO.accessM[InfrastructureConfiguration](_.db.db)
        r  <- ZIO.fromFuture(ec => db.run(dbio))
      } yield r

    def fromStreamingDBIO[T](dbio: StreamingDBIO[_, T]): ZIO[InfrastructureConfiguration, Throwable, ZStream[Any, Throwable, T]] =
      for {
        db <- ZIO.accessM[InfrastructureConfiguration](_.db.db)
        r  <- ZIO.effect(db.stream(dbio).toStream())
      } yield r
  }

}
