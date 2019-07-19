package io.github.mvillafuertem.slick.stream


trait UserRepository[F[_], T] {

  def findAll(): F[Seq[T]]

  def findById(id: Long): F[Option[T]]

  def insert(t: T): F[Long]

  def delete(id: Long): F[Int]

}
