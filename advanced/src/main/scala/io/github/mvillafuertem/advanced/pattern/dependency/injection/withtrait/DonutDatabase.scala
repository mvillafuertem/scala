package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

trait DonutDatabase[A] {

  def addOrUpdate(donut: A): Long

  def query(donut: A): A

  def delete(donut: A): Boolean
}