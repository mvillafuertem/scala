package io.github.mvillafuertem.zio.akka.cluster.domain

trait Domain {

  sealed trait ChatMessage

  case class Message(name: String, msg: String) extends ChatMessage

  case class Join(name: String) extends ChatMessage

  case class Leave(name: String) extends ChatMessage

}
