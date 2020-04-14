package io.github.mvillafuertem.sensor.controller

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import com.lightbend.stream.messages.messages.{HeaterCommand, HeaterControl}
import com.lightbend.streams.config.StreamsConfig._
import com.lightbend.streams.transform.{DataTransformer, MayBeHeaterControl}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.duration._
import scala.util.Success

object AkkaTemperatureController {

  // Initialization

  implicit val controllerManager = ActorSystem(
    Behaviors.setup[ControllerManagerActorTyped](
      context => new ControllerManagerBehavior(context)), "ControllerManager")

  implicit val materializer = Materializer(controllerManager)
  implicit val executionContext = controllerManager.executionContext
  implicit val askTimeout = Timeout(30.seconds)

  // Sources
  val sensorSettings = ConsumerSettings(controllerManager.toClassic, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(kafkaConfig.brokers)
    .withGroupId(kafkaConfig.heateroutputgroup)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val controlSettings = ConsumerSettings(controllerManager.toClassic, new ByteArrayDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(kafkaConfig.brokers)
    .withGroupId(kafkaConfig.temperaturesetgroup)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  // Sink
  val heaterSettings =
    ProducerSettings(controllerManager.settings.config.getConfig("akka.kafka.producer"), new ByteArraySerializer, new ByteArraySerializer)
      .withBootstrapServers(kafkaConfig.brokers)

  def main(args: Array[String]): Unit = {

    println(s"Akka Temperature controller, brokers ${kafkaConfig.brokers}")
    println(s"Input queue sensor ${kafkaConfig.heateroutputtopic}, input group ${kafkaConfig.heateroutputgroup} ")
    println(s"Input queue control ${kafkaConfig.temperaturesettopic}, input group ${kafkaConfig.temperaturesetgroup} ")
    println(s"Output queue ${kafkaConfig.heaterinputtopic}")

    // Control stream processing
    Consumer.atMostOnceSource(controlSettings, Subscriptions.topics(kafkaConfig.temperaturesettopic))
      .map(record => DataTransformer.controlFromByteArray(record.value)).collect { case Success(a) => a }
      .via(ActorFlow.ask(1)(controllerManager)((elem, replyTo : ActorRef[Done]) => new TemperatureSetting(replyTo, elem)))
      .runWith(Sink.ignore) // run the stream, we do not read the results directly

    // Sensor stream processing
    Consumer.atMostOnceSource(sensorSettings, Subscriptions.topics(kafkaConfig.heateroutputtopic))
      .map(record => DataTransformer.sensorFromByteArray(record.value)).collect { case Success(a) => a }
      .via(ActorFlow.ask(1)(controllerManager)((elem, replyTo : ActorRef[MayBeHeaterControl]) => new SensorDataRequest(replyTo, elem)))
      .map(result => {
        result.sensorID match {
          case Some(value) =>
            println(s"sending new control ${result.command} for sensor $value")
            Some(HeaterControl(value, HeaterCommand.fromValue(result.command)))
          case _ =>
            None
        }
      })
      .filter(_.isDefined)
      .map(value => new ProducerRecord[Array[Byte], Array[Byte]](kafkaConfig.heaterinputtopic, DataTransformer.toByteArray(value.get)))
      .runWith(Producer.plainSink(heaterSettings))
  }
}
