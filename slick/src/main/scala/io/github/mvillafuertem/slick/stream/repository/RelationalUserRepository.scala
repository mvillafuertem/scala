package io.github.mvillafuertem.slick.stream.repository

import io.github.mvillafuertem.slick.stream.UserRepository
import io.github.mvillafuertem.slick.stream.domain.User
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

final class RelationalUserRepository(databaseConfig: DatabaseConfig[JdbcProfile]) extends UserRepository[Future, User] {

  private[repository] val profile = databaseConfig.profile

  import profile.api._

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {

    // P R I M A R Y  K E Y
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    // C O L U M N S
    def userId= column[Long]("user_id")

    def name= column[String]("name")

    def username= column[String]("username")

    // D E F A U L T  P R O J E C T I O N
    def * = (userId, name, username, id.?).mapTo[User]

    // I N D E X
    def idUserIdIndex = index("id_userid_index", (id, userId), unique = true)
  }

  val userTable =  TableQuery[UserTable]

  implicit def executeFromDb[A](action: DBIO[A]): Future[A] = databaseConfig.db.run(action)

  override def findAll(): Future[Seq[User]] = userTable.result

  override def findById(id: Long): Future[Option[User]] = userTable.filter(_.id === id).result.headOption

  override def insert(user: User): Future[Long] = userTable returning userTable.map(_.id) += user

  override def delete(id: Long): Future[Int] = userTable.filter(_.id === id).delete

  def schema(): profile.DDL = userTable.schema

}
