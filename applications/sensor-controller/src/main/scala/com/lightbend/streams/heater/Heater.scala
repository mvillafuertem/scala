package com.lightbend.streams.heater

import java.io.ByteArrayOutputStream

import com.lightbend.stream.messages.messages.{HeaterControl, SensorData, TemperatureControl}
import com.lightbend.streams.config.StreamsConfig._
import com.lightbend.streams.kafka.{KafkaLocalServer, MessageListener, MessageSender, RecordProcessorTrait}
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

object Heater {

  var heaterOperation = 1
  val sensorID = 12345

  def main(args: Array[String]) {

    val brokers = kafkaConfig.brokers
    val temperatureUp = Duration(temperatureUpRate.rate)
    val temperatureDown = Duration(temperatureDownRate.rate)
    val timeInterval = Duration(sensorPublishInterval.interval)

    print(s"Starting Heater: kafka $brokers, sending sensor data to ${kafkaConfig.heateroutputtopic}, sending control to " +
      s"${kafkaConfig.temperaturesettopic} temperature up rate $temperatureUp, temperature down rate $temperatureDown, " +
      s" sensor publish interval $timeInterval")

    // Create kafka broker
    val kafka = KafkaLocalServer(true)
    kafka.start()
    println(s"Kafka Cluster created")

    // Create sender
    val sender = MessageSender[Array[Byte], Array[Byte]](kafkaConfig.brokers)

    // Start listener for control signal
    val listener = MessageListener(brokers, kafkaConfig.heaterinputtopic, kafkaConfig.heatersourcegroup, new ControlProcessor())
    listener.start()

    // Start control publishing
    controltemperature(sender)

    // Loop producing temperature reading
    val bos = new ByteArrayOutputStream
    var temperature = 42.0
    while(true){
      // Calculate output temperature
      heaterOperation match {
        case 0 => // Heater is on - increase temperature
          temperature = temperature + timeInterval.toMillis.toDouble/temperatureUp.toMillis.toDouble
        case _ => // Heater is off - decrease temperature
          temperature = temperature - timeInterval.toMillis.toDouble/temperatureDown.toMillis.toDouble
      }
      // Send it to Kafka
      val sd = new SensorData(sensorID, temperature)
      bos.reset()
      sd.writeTo(bos)
      sender.writeValue(kafkaConfig.heateroutputtopic, bos.toByteArray)
      println(s"Send sensor data $sd")
      // Pause
      pause(timeInterval)
    }
  }

  def pause(timeInterval : Duration): Unit = Thread.sleep(timeInterval.toMillis)

  // Changing desired state
  def controltemperature(sender : MessageSender[Array[Byte], Array[Byte]]) : Future[Unit] = Future{
    var desired = 45.0
    val controlInterval = 10.minute
    val generator = new Random
    val bos = new ByteArrayOutputStream
    while(true) {
      val temperatureControl = TemperatureControl(sensorID, desired, 1.0, 1.0)
      bos.reset()
      temperatureControl.writeTo(bos)
      sender.writeValue(kafkaConfig.temperaturesettopic, bos.toByteArray)
      println(s"Send new temperature control $temperatureControl")
      desired = desired + (generator.nextInt(10) - 5)
      pause(controlInterval)
    }
  }
}

// Control message listener
class ControlProcessor extends RecordProcessorTrait[Array[Byte], Array[Byte]] {

  import Heater._

  override def processRecord(record: ConsumerRecord[Array[Byte], Array[Byte]]): Unit = {
    try{
      heaterOperation = HeaterControl.parseFrom(record.value()).command.index
      println(s"Updated heater control to $heaterOperation")
    }
    catch {
      case t: Throwable => println(s"Error reading heater control $t")
    }
  }
}