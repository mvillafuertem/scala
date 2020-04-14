package io.github.mvillafuertem.sensor.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.lightbend.streams.transform.MayBeHeaterControl

class ControllerManagerBehavior (context: ActorContext[ControllerManagerActorTyped]) extends AbstractBehavior[ControllerManagerActorTyped](context) {

  println("Creating controller manager")

  // Get (maybe) controller actor based on sensorID
  private def getSensorActorMayBe(sensorID : Int): Option[ActorRef[ControllerActorTyped]] = {

    val res = context.child(sensorID.toString)
    if(res.isDefined)
      Some(res.get.asInstanceOf[ActorRef[ControllerActorTyped]])
    else
      None
  }

  // Get (or create) controller actor based on sensorID
  private def getSensorActor(sensorID : Int): ActorRef[ControllerActorTyped] = {

    getSensorActorMayBe(sensorID) match {
      case Some(actorRef) => actorRef
      case _ => context.spawn(Behaviors.setup[ControllerActorTyped](context => new ControllerBehaviour(context, sensorID)), sensorID.toString)
    }
  }

  override def onMessage(msg: ControllerManagerActorTyped): Behavior[ControllerManagerActorTyped] = {
    msg match {
      case setting: TemperatureSetting => // change temperature settings
        getSensorActor(setting.record.sensorID) tell setting
      case sensor: SensorDataRequest =>   // process sensor reading
        getSensorActorMayBe(sensor.record.sensorID) match {
          case Some(actorRef) => actorRef tell sensor
          case _ => sensor.reply ! MayBeHeaterControl(None, 0)
        }
    }
    this
  }
}
