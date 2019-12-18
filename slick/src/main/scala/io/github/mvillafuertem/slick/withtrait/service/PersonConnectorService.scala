package io.github.mvillafuertem.slick.withtrait.service

import io.github.mvillafuertem.slick.withtrait.model.{Connection, ConnectionId, EdgeDBO, VertexDBO}
import io.github.mvillafuertem.slick.withtrait.{EdgeRepository, PersonConnector, VertexRepository}

import scala.concurrent.Future

final class PersonConnectorService(vertexRepository: VertexRepository[Future, VertexDBO],
                      edgeRepository: EdgeRepository[Future, EdgeDBO]) extends PersonConnector[Future] {

  override def create(connection: Connection): Future[Connection] = ???

  override def findByAssetComposerId(connectionId: ConnectionId): Future[Connection] = ???

  override def update(connection: Connection): Future[Connection] = ???

  override def delete(connectionId: ConnectionId): Future[Int] = ???

}