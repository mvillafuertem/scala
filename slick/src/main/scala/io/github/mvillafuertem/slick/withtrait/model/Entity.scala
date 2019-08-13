package io.github.mvillafuertem.slick.withtrait.model

trait Entity[T, I] {
  val id: Option[I]
}
