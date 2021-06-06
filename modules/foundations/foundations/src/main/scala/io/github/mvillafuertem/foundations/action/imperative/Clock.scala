package io.github.mvillafuertem.foundations.action.imperative

import java.time.Instant

trait Clock {

  def now(): Instant

}

object Clock {

  val system: Clock = () => Instant.now()

  def constant(instant: Instant): Clock = () => instant

}
