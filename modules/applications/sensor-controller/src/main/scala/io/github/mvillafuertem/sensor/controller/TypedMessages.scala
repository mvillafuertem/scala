package io.github.mvillafuertem.sensor.controller

import akka.Done
import akka.actor.typed.ActorRef
import com.lightbend.stream.messages.messages.{SensorData, TemperatureControl}
import com.lightbend.streams.transform.MayBeHeaterControl

// Controller
trait ControllerActorTyped
case class SensorDataRequest(reply: ActorRef[MayBeHeaterControl], record : SensorData) extends ControllerActorTyped with ControllerManagerActorTyped
case class TemperatureSetting(reply: ActorRef[Done], record : TemperatureControl) extends ControllerActorTyped with ControllerManagerActorTyped

// Controller manager
trait ControllerManagerActorTyped
