package io.github.mvillafuertem.tapir.api

import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.github.mvillafuertem.tapir.api.ProductsEndpoint.ProductsQuery
import io.github.mvillafuertem.tapir.api.mapper.ProductsMapper
import io.github.mvillafuertem.tapir.domain.model.{ Product, ProductType }
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.generic.auto._

/**
 * @author
 *   Miguel Villafuerte
 */
trait ProductsEndpoint extends ProductsMapper {

  // i n f o r m a t i o n
  private[api] lazy val apiResource: String                  = "api"
  private[api] lazy val apiVersion: String                   = "v1.0"
  private[api] lazy val apiNameResource: String              = "api-resource"
  private[api] lazy val apiDescriptionResource: String       = "Api Resources"
  private[api] lazy val baseApiResource: EndpointInput[Unit] = apiResource / apiVersion

  // e n d p o i n t
  private[api] lazy val baseEndpoint: Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint
      .tag("Products")
      .in(baseApiResource)
      .name(apiNameResource)
      .description(apiDescriptionResource)

  // i n f o r m a t i o n
  private[api] lazy val productsResource                            = "products"
  private[api] lazy val productsResourceName: String                = "products-resource"
  private[api] lazy val productsResourceDescription: String         = "Get all products"
  private[api] lazy val limitParameter                              = query[Option[Int]]("limit").description("Maximum number of products to retrieve")
  private[api] lazy val offsetParameter                             = query[Option[Int]]("offset").description("Position the initial product to retrieve")
  private[api] lazy val productsQuery: EndpointInput[ProductsQuery] =
    path[ProductType]("type")
      .map(Option(_))(_.get)
      .and(offsetParameter)
      .and(limitParameter)
      .mapTo[ProductsQuery]

  private[api] lazy val baseProductsResource = productsResource / productsQuery

  // e n d p o i n t
  private[api] lazy val productsEndpoint: Endpoint[Unit, ProductsQuery, Unit, Source[ByteString, Any], Any with AkkaStreams] =
    baseEndpoint.get
      .in(baseProductsResource)
      .name(productsResourceName)
      .description(productsResourceDescription)
      .out(streamBody(AkkaStreams)(Schema.derived[List[Product]], CodecFormat.Json()))

  // e x a m p l e

  // private val vectorProductsExample: Vector[Product] = Vector(Product(ProductId(), "", New))

}

object ProductsEndpoint extends ProductsEndpoint {

  // d a t a  t r a n s f e r  o b j e c t
  type Limit  = Option[Int]
  type Offset = Option[Int]
  case class ProductsQuery(productType: Option[ProductType], offset: Offset, limit: Limit)

}
