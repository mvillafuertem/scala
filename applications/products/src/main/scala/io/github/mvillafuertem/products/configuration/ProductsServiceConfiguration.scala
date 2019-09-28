package io.github.mvillafuertem.products.configuration
import slick.basic.BasicBackend
import zio.{UIO, ZIO}
import slick.jdbc.H2Profile.backend._

final class ProductsServiceConfiguration extends InfrastructureConfiguration {


  override def db: InfrastructureConfiguration.Service = new InfrastructureConfiguration.Service {
    override def db: UIO[BasicBackend#DatabaseDef] = ZIO.effectTotal(Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver="org.h2.Driver"))
  }

}
