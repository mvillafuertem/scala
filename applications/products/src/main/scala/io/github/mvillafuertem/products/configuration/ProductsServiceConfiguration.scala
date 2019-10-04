package io.github.mvillafuertem.products.configuration
import io.github.mvillafuertem.products.api.SwaggerApi
import slick.basic.BasicBackend
import zio.{UIO, ZIO}
import slick.jdbc.H2Profile.backend._

trait ProductsServiceConfiguration extends InfrastructureConfiguration {


  lazy val productsConfigurationProperties = ProductsConfigurationProperties()

  override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forConfig("infrastructure.h2"))

  val routes = SwaggerApi.route

}
