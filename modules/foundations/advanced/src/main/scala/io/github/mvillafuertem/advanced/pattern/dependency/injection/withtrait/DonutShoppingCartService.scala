package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

class DonutShoppingCartService[T](
  donutInventoryService: DonutInventoryService[T],
  donutPricingService: DonutPricingService[T],
  donutOrderService: DonutOrderService[T]
) {

  def bookOrder(donut: T, quantity: Int): Int = {
    println("DonutShoppingCartService->bookOrder")

    donutInventoryService.checkStock(donut) match {
      case true  =>
        val price = donutPricingService.calculatePrice(donut)
        donutOrderService.createOrder(donut, quantity, price) // the id of the booked order

      case false =>
        println(s"Sorry donut $donut is out of stock!")
        -100 // return some error code to identify out of stock
    }
  }
}
