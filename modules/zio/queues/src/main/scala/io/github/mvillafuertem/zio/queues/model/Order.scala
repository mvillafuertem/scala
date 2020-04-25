package io.github.mvillafuertem.zio.queues.model

sealed trait Order

object Order {

  case object Coffee extends Order

  case object Sandwich extends Order

  case object Bacon extends Order

}
