package io.github.mvillafuertem.sensor.controller

import akka.Done
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import com.lightbend.stream.messages.messages.TemperatureControl
import com.lightbend.streams.transform.MayBeHeaterControl

class ControllerBehaviour (context: ActorContext[ControllerActorTyped], sensorID : Int) extends AbstractBehavior[ControllerActorTyped](context) {

  println(s"Creating controller for sensor $sensorID")

  private var currentSetting: Option[TemperatureControl] = None
  private var previousCommand = -1

  override def onMessage(msg: ControllerActorTyped): Behavior[ControllerActorTyped] = {
    msg match {
      case setting: TemperatureSetting => // change temperature settings
        println(s"New temperature settings ${setting.record}")
        currentSetting = Some(setting.record)
        setting.reply ! Done
      case sensor: SensorDataRequest =>   // process sensor reading
        currentSetting match {
          case Some(setting) => // We are controlling
            (if(sensor.record.temperature > (setting.desired + setting.upDelta)) 1
                else if(sensor.record.temperature < (setting.desired - setting.downDelta)) 0 else -1) match {
              case action if(action < 0) => sensor.reply ! MayBeHeaterControl(None, 0)
              case action =>
                if(previousCommand != action) {
                  previousCommand = action
                  sensor.reply ! MayBeHeaterControl(Some(sensor.record.sensorID), action)
                }
                else sensor.reply ! MayBeHeaterControl(None, 0)
            }
          case _ => // No control
            sensor.reply ! MayBeHeaterControl(None, 0)
        }
    }
    this
  }
}
