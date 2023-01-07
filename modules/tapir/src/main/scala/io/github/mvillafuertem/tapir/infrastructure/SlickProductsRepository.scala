package io.github.mvillafuertem.tapir.infrastructure

import io.github.mvillafuertem.tapir.configuration.InfrastructureConfiguration
import io.github.mvillafuertem.tapir.domain.error.ProductException
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductId }
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import io.github.mvillafuertem.tapir.infrastructure.tables.ProductTable
import slick.dbio.{ DBIO, StreamingDBIO }
import slick.jdbc.H2Profile.api._
import zio.interop.reactivestreams._
import zio.stream.ZStream
import zio.{ stream, IO, UIO, ZIO, ZLayer }

import scala.concurrent.ExecutionContext.Implicits.global

// type projection in scala3 https://docs.scala-lang.org/scala3/reference/dropped-features/type-projection.html
// type T[A] = IO[ProductException, A]
// trait SlickProductsRepository extends ProductsRepository[T] with InfrastructureConfiguration
trait SlickProductsRepository extends ProductsRepository[({ type T[A] = IO[ProductException, A] })#T] with InfrastructureConfiguration {
  self =>

  import SlickProductsRepository._

  val products = TableQuery[ProductTable.Products]

  override def create(product: Product): IO[ProductException, ProductId] = {
    val insert = (products += product).map(_ => product.productId)
    ZIO.fromDBIO(insert).provideLayer(ZLayer.succeed(self)).refineOrDie { case e: Exception =>
      new ProductException(e)
    }
  }

  override def getAll: stream.Stream[ProductException, Product] = {
    val getAll: StreamingDBIO[Seq[Product], Product] = products.result
    ZStream.fromStreamingDBIO(getAll).provideLayer(ZLayer.succeed(self)).refineOrDie { case e: Exception =>
      new ProductException(e)
    }
  }

  override def find: IO[ProductException, String] = UIO.succeed("hola")

}

object SlickProductsRepository {

  implicit class ZIOObjOps(private val obj: ZIO.type) extends AnyVal {
    def fromDBIO[R](dbio: DBIO[R]): ZIO[InfrastructureConfiguration, Throwable, R] =
      for {
        db <- obj.environmentWithZIO[InfrastructureConfiguration](_.get.db)
        r  <- obj.fromFuture(_ => db.run(dbio))
      } yield r
  }

  implicit class ZStreamsObjOps(private val obj: ZStream.type) extends AnyVal {
    def fromStreamingDBIO[T](dbio: StreamingDBIO[_, T]): ZStream[InfrastructureConfiguration, Throwable, T] =
      for {
        db <- obj.environmentWithZIO[InfrastructureConfiguration](_.get.db)
        r  <- db.stream(dbio).toStream()
      } yield r
  }

}
