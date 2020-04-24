package io.github.mvillafuertem.slick.withtrait

/**
 * @author Miguel Villafuerte
 */
trait VertexRepository[F[_], T] extends BaseRepository[F, T] {

  def findByCode(tenantId: Long, assetId: Long): F[T]

}
