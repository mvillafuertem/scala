package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

class DonutOrderService[T] {
  def createOrder(donut: T, quantity: Int, price: Double): Int = {
    println(s"Saving donut order to database: donut = $donut, quantity = $quantity, price = $price")
    100 // the id of the booked order
  }
}
