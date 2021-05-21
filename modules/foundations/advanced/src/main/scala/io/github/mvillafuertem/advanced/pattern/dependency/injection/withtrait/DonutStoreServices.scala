package io.github.mvillafuertem.advanced.pattern.dependency.injection.withtrait

trait DonutStoreServices {
  val donutInventoryService    = new DonutInventoryService[String]
  val donutPricingService      = new DonutPricingService[String]
  val donutOrderService        = new DonutOrderService[String]
  val donutShoppingCartService = new DonutShoppingCartService(donutInventoryService, donutPricingService, donutOrderService)
}
