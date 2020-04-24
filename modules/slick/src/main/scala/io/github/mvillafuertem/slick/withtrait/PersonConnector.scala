package io.github.mvillafuertem.slick.withtrait

import io.github.mvillafuertem.slick.withtrait.model.{ Connection, ConnectionId }

import scala.concurrent.Future

trait PersonConnector[F[_]] {

  def create(connection: Connection): Future[Connection]

  def findByAssetComposerId(connectionId: ConnectionId): Future[Connection]

  def update(connection: Connection): Future[Connection]

  def delete(connectionId: ConnectionId): Future[Int]

}
