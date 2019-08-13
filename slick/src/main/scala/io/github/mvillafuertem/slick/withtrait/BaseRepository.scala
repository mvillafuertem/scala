package io.github.mvillafuertem.slick.withtrait

/**
 * @author Miguel Villafuerte
 */
trait BaseRepository[F[_], T] {

  def findAll(): F[Seq[T]]

  /**
    * Retrieve the entity option
    *
    * @param id identifier for the entity to found
    * @return Some(e) or None
    */
  def findById(id: Long): F[Option[T]]

  /**
    * Retrieve the repository entity count
    *
    * @return number of entities in the repo
    */
  def count(): F[Int]

  /**
    * Inserts the entity returning the generated identifier
    *
    * @param t entity to be added
    * @return entity id after the insert
    */
  def insert(t: T): F[Long]

  /**
    * Inserts a sequence of entities returning the generated sequence of identifiers
    *
    * @param seq entity sequence
    * @return generated identifier sequence after the insert
    */
  def insert(seq: Seq[T]): F[Seq[Long]]

  /**
    * Updates the entity in the repository
    *
    * @param t entity to be updated
    * @return number of entities updated
    */
  def update(t: T): F[Int]

  /**
    * Deletes the entity with the identifier supplied
    *
    * @param id entity identifier
    * @return number of entites affected
    */
  def delete(id: Long): F[Int]

  /**
    * Deletes all the entities from the repository
    *
    * @return number of entites affected
    */
  def deleteAll(): F[Int]

}
