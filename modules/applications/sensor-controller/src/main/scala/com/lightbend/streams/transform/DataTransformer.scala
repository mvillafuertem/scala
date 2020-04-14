package com.lightbend.streams.transform

import java.io.ByteArrayOutputStream

import com.lightbend.stream.messages.messages.{HeaterControl, SensorData, TemperatureControl}

import scala.util.Try

object DataTransformer {

  val bos = new ByteArrayOutputStream

  def controlFromByteArray(message: Array[Byte]): Try[TemperatureControl] = Try {
    TemperatureControl.parseFrom(message)
  }

  def sensorFromByteArray(message: Array[Byte]): Try[SensorData] = Try {
    SensorData.parseFrom(message)
  }

  def toByteArray(control : HeaterControl): Array[Byte] = {
    bos.reset()
    control.writeTo(bos)
    bos.toByteArray
  }
}

// Control
case class MayBeHeaterControl(sensorID : Option[Int], command : Int)
