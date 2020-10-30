package io.github.mvillafuertem.aws.lambda

import java.io.{ InputStream, OutputStream }

import com.amazonaws.services.lambda.runtime.{ Context, RequestStreamHandler }
import io.circe.jawn.decode
import io.circe.syntax._
import io.github.mvillafuertem.aws.lambda.SampleLambda.WeatherData

import scala.io.Source

// @see https://yeghishe.github.io/2016/10/16/writing-aws-lambdas-in-scala.html
//      https://edward-huang.com/aws/cloud/2019/11/28/how-to-setup-aws-lambda-in-scala-without-any-external-library/
//final class SampleLambda extends RequestHandler[WeatherData, WeatherData] {
//
//  override def handleRequest(event: WeatherData, context: Context): WeatherData = {
//    val logger = context.getLogger
//    logger.log(s"Event ~ ${event.asJson.noSpaces}")
//    logger.log(s"Event Type ~ ${event.getClass.toString}")
//    event
//  }
//
//}
//
//object SampleLambda {
//
//  class WeatherData(
//    @BeanProperty var temperatureK: Int,
//    @BeanProperty var windKmh: Int,
//    @BeanProperty var humidityPct: Double,
//    @BeanProperty var pressureHPa: Int
//  ) {
//    def this() = this(0, 0, 0.0, 0)
//  }
//
//  object WeatherData {
//    def apply(temperatureK: Int, windKmh: Int, humidityPct: Double, pressureHPa: Int): WeatherData =
//      new WeatherData(temperatureK, windKmh, humidityPct, pressureHPa)
//  }
//
//}

class SampleLambda extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val inputString: String = Source.fromInputStream(input).mkString

    decode[WeatherData](inputString).map { requestFormatter =>
      println(s"${requestFormatter} is here")
      // do something .....

      output.write(requestFormatter.asJson.noSpaces.toCharArray.map(_.toByte))
    }
  }
}

object SampleLambda {

  import io.circe._
  import io.circe.generic.semiauto._

  case class WeatherData(temperatureK: Int, windKmh: Int, humidityPct: Double, pressureHPa: Int)

  object WeatherData {
    implicit val encode: Encoder[WeatherData] = deriveEncoder[WeatherData]
    implicit val decode: Decoder[WeatherData] = deriveDecoder[WeatherData]
  }

}
