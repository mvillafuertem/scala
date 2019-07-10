package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

class DonutInventoryService[T] {
  def checkStock(donut: T): Boolean = {
    println("DonutInventoryService->checkStock")
    true
  }
}
