package io.github.mvillafuertem.tapir.api

import io.circe.syntax._
import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.github.mvillafuertem.tapir.domain.model.ProductType
import io.github.mvillafuertem.tapir.domain.model.ProductType.{ New, Used }
import sttp.tapir.Codec.{ JsonCodec, PlainCodec }
import sttp.tapir.json.circe._
import sttp.tapir.{ Codec, Validator }
import io.github.mvillafuertem.tapir.domain.model.ProductType
import io.github.mvillafuertem.tapir.domain.model.ProductType.New
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Schema, Validator}

/**
 * @author
 *   Miguel Villafuerte
 */
trait ProductsCodec {

  // p r o d u c t  t y p e  c o d e c
  implicit def productTypeValidator: Validator.Enumeration[ProductType] = Validator.enumeration(ProductType.productTypes.toList)
  implicit def productTypeSchema: Schema[ProductType] = Schema.derivedEnumeration[ProductType](encode = Some(_.toString.toLowerCase))
  
  implicit def plainCodecForProductType: PlainCodec[ProductType] =
    Codec.string
      .map[ProductType]((_: String) match {
        case "new"  => New
        case "used" => Used
      })(_.toString.toLowerCase)
      .validate(productTypeValidator)

  private[api] implicit lazy val productTypeCodec: JsonCodec[ProductType] =
    implicitly[JsonCodec[Json]].map(json =>
      json.as[ProductType](decodeProductType) match {
        case Right(value) => value
        case Left(value)  => throw value
      }
    )(productType => productType.asJson(encodeProductType))

  private[api] implicit lazy val decodeProductType: Decoder[ProductType] = (c: HCursor) =>
    for {
      productType <- c.get[String]("productType")
    } yield ProductType.find(productType)

  private[api] implicit lazy val encodeProductType: Encoder[ProductType] =
    (s: ProductType) => Json.fromString(s.toString.toLowerCase)

}
