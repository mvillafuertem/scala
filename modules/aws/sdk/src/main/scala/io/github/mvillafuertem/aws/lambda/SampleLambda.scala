package io.github.mvillafuertem.aws.lambda

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import io.github.mvillafuertem.aws.lambda.SampleLambda.WeatherData

final class SampleLambda extends RequestHandler[WeatherData, WeatherData] {

  override def handleRequest(event: WeatherData, context: Context): WeatherData = {
    val logger = context.getLogger
    logger.log(s"Event ~ $event")
    logger.log(s"Event Type ~ ${event.getClass.toString}")
    event
  }

}

object SampleLambda {

  case class WeatherData(temperatureK: Int, windKmh: Int, humidityPct: Double, pressureHPa: Int)

}
