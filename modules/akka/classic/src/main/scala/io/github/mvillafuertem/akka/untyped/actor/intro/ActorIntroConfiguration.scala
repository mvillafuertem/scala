package io.github.mvillafuertem.akka.untyped.actor.intro

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import com.typesafe.config.ConfigFactory

object ActorIntroConfiguration extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // Inline configuration
  val configString =
    """
      | akka {
      |   loglevel = "ERROR"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ActorIntroConfiguration", config)
  val actor  = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"

  // Configuration Filesystem
  val defaultConfigFileSystem     = ActorSystem("DefaultConfigFileSystem")
  val defaultConfigActor          = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "Remember me"

  // Separate config in the same file
  val separateConfigInTheSameFile = ConfigFactory.load().getConfig("myConfig")

  // Separate config in another file
  val configInAnotherFile = ConfigFactory.load("anotherFolder/myConfig.conf")

  // Config in different files format
  // - JSON
  // - Properties
  // - YAML
  val configInAnotherFileFormat = ConfigFactory.load("anotherFolder/myConfig.yaml")

}
