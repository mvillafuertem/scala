package com.lightbend.streams.config

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._


case class KafkaConfig(brokers: String = "localhost:9092",
                       heaterinputtopic: String = "heatercontrol", heatersourcegroup: String = "HeaterControlGroup",
                       heateroutputtopic: String = "sensor", heateroutputgroup: String = "sensorGroup",
                       thermostattopic: String = "thermostat", thermostatgroup: String = "thermostatServingGroup",
                       temperaturesettopic: String = "tempset", temperaturesetgroup: String = "tempsetGroup")

case class TemperatureUpRate(rate : String = "1.minute")
case class TemperatureDownRate(rate : String = "2.minute")
case class SensorPublishInterval(interval : String = "10.second")
case class FlinkConfiguration(checkpointingInterval : Long = 5000 /*milliseconds*/, parallelism : Int = 2, port : Int = 6124)
case class SparkConfiguration(checkpointingDir : String = "chk")


object StreamsConfig {

  val config = ConfigFactory.load()

  val kafkaConfig =
    try {
      config.as[KafkaConfig]("kafkaConfig")
    }
    catch {
      case _: Throwable => KafkaConfig()
    }

  val temperatureUpRate =
    try {
      config.as[TemperatureUpRate]("temperatureUpRate")
    }
    catch {
      case _: Throwable => TemperatureUpRate()
    }

  val temperatureDownRate =
    try {
      config.as[TemperatureDownRate]("temperatureDownRate")
    }
    catch {
      case _: Throwable => TemperatureDownRate()
    }

  val sensorPublishInterval =
    try {
      config.as[SensorPublishInterval]("sensorPublishInterval")
    }
    catch {
      case _: Throwable => SensorPublishInterval()
    }

  val flinkConfig =
    try {
      config.as[FlinkConfiguration]("flinkConfiguration")
    }
    catch {
      case _: Throwable => FlinkConfiguration()
    }

  val sparkConfig =
    try {
      config.as[SparkConfiguration]("sparkConfiguration")
    }
    catch {
      case _: Throwable => SparkConfiguration()
    }
}
