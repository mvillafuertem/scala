package io.github.mvillafuertem.tapir.api.routes

import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import io.github.mvillafuertem.tapir.ProductsServiceApplication.runtime
import io.github.mvillafuertem.tapir.api.ProductsEndpoint
import io.github.mvillafuertem.tapir.api.ProductsEndpoint.ProductsQuery
import io.github.mvillafuertem.tapir.domain.repository.ProductsRepository
import org.reactivestreams.Publisher
import sttp.tapir.server.akkahttp._
import zio.interop.reactivestreams._

import scala.concurrent.Future

final class ProductsRoute[F[_]](productsRepository: ProductsRepository[F]) extends ProductsEndpoint {

  val route: Route = AkkaHttpServerInterpreter()
    .toRoute(
      productsEndpoint.serverLogic[Future] { case ProductsQuery(_, _, _) =>
        val value: Publisher[ByteString] = runtime.unsafeRun(
          productsRepository.getAll
            .map(_.asJson.noSpaces)
            .map(query => ByteString(query) ++ ByteString("\n"))
            .toPublisher
        )
        Future.successful(
          Right(
            Source
              .fromPublisher(value)
              .intersperse(ByteString("["), ByteString(","), ByteString("]"))
          )
        )
      }
    )
}
