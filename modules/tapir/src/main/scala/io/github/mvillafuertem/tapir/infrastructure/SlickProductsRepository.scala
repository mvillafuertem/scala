package io.github.mvillafuertem.tapir.infrastructure

import io.github.mvillafuertem.tapir.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductId }
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import io.github.mvillafuertem.tapir.infrastructure.tables.ProductTable
import slick.dbio.{ DBIO, StreamingDBIO }
import slick.driver.H2Driver.api._
import zio.interop.reactivestreams._
import zio.stream.ZStream
import zio.{ IO, UIO, ZIO }

import scala.concurrent.ExecutionContext.Implicits.global

trait SlickProductsRepository extends ProductsRepository with InfrastructureConfiguration {
  self =>

  import SlickProductsRepository._

  val products = TableQuery[ProductTable.Products]

  override def create(product: Product): IO[ProductException, ProductId] = {
    val insert = (products += product).map(_ => product.productId)
    ZIO.fromDBIO(insert).provide(self).refineOrDie {
      case e: Exception => new ProductException(e)
    }
  }

  override def getAll: IO[ProductException, ZStream[Any, Throwable, Product]] = {
    val getAll: StreamingDBIO[Seq[Product], Product] = products.result
    ZIO.fromStreamingDBIO(getAll).provide(self).refineOrDie {
      case e: Exception => new ProductException(e)
    }
  }

  override def find: IO[Unit, String] = UIO.succeed("hola")

}

object SlickProductsRepository {

  implicit class ZIOObjOps(private val obj: ZIO.type) extends AnyVal {
    def fromDBIO[R](dbio: DBIO[R]): ZIO[InfrastructureConfiguration, Throwable, R] =
      for {
        db <- ZIO.accessM[InfrastructureConfiguration](_.db)
        r  <- ZIO.fromFuture(_ => db.run(dbio))
      } yield r

    def fromStreamingDBIO[T](dbio: StreamingDBIO[_, T]): ZIO[InfrastructureConfiguration, Throwable, ZStream[Any, Throwable, T]] =
      for {
        db <- ZIO.accessM[InfrastructureConfiguration](_.db)
        r  <- ZIO.effect(db.stream(dbio).toStream())
      } yield r
  }

}
