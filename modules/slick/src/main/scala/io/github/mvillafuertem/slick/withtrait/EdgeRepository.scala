package io.github.mvillafuertem.slick.withtrait

/**
 * @author
 *   Miguel Villafuerte
 */
trait EdgeRepository[F[_], T] extends BaseRepository[F, T] {

  def findByStartVertexId(startVertexId: Long): F[Seq[T]]

  def findByEndVertexId(endVertexId: Long): F[Seq[T]]

  def findByStartAndEndVertexIds(startVertexId: Long, endVertexId: Long): F[T]

}
