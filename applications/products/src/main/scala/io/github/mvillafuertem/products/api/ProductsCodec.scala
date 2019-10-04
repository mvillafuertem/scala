package io.github.mvillafuertem.products.api

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import io.github.mvillafuertem.products.domain.model.ProductType
import io.github.mvillafuertem.products.domain.model.ProductType.{New, Used}
import tapir.Codec.{JsonCodec, PlainCodec}
import tapir.json.circe._
import tapir.{Codec, Validator}


/**
 * @author Miguel Villafuerte
 */
trait ProductsCodec {

  // p r o d u c t  t y p e  c o d e c
  implicit def plainCodecForProductType: PlainCodec[ProductType] = {
    Codec.stringPlainCodecUtf8.map[ProductType]({
      case "new"  => New
      case "used" => Used
  })(_.toString.toLowerCase)
    .validate(Validator.enum)
  }

  private[api] implicit lazy val productTypeCodec: JsonCodec[ProductType] =
    implicitly[JsonCodec[Json]].map(json => json.as[ProductType](decodeProductType) match {
      case Right(value) => value
    })(productType => productType.asJson(encodeProductType))

  private[api] implicit lazy val decodeProductType: Decoder[ProductType] = (c: HCursor) => for {
    productType <- c.get[String]("productType")
  } yield ProductType.find(productType)

  private[api] implicit lazy val encodeProductType: Encoder[ProductType] =
    (s: ProductType) => Json.fromString(s.toString.toLowerCase)

}
