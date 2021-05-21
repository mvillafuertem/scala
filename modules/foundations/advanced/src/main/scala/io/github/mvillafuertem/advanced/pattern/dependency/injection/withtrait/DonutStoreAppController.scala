package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

trait DonutStoreAppController {
  this: DonutStoreServices =>

  def bookOrder(donut: String, quantity: Int): Int = {
    println("DonutStoreAppController->bookOrder")
    donutShoppingCartService.bookOrder(donut, quantity)
  }
}
