package io.github.mvillafuertem.aws.lambda

import java.util

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }

// final class SampleLambda extends RequestHandler[WeatherData, WeatherData] { @see https://github.com/localstack/localstack/issues/1634
final class SampleLambda extends RequestHandler[util.Map[String, Any], util.Map[String, Any]] {

  override def handleRequest(event: util.Map[String, Any], context: Context): util.Map[String, Any] = {
    val logger = context.getLogger
    logger.log(s"Event ~ $event")
    logger.log(s"Event Type ~ ${event.getClass.toString}")
    event
  }

}

object SampleLambda {

  case class WeatherData(temperatureK: Int, windKmh: Int, humidityPct: Double, pressureHPa: Int)

}
