package neko.chat.repository

import java.time.Clock
import java.util.UUID
import java.sql.Timestamp

import neko.core.jdbc.{DBPool, ConnectionIO}
import neko.core.jdbc.query._
import neko.chat.entity.User
import java.sql.ResultSet

class UserRepositoryImpl(pool: DBPool, clock: Clock) extends UserRepository {

  override def insert(name: String): Either[Throwable, User] = {
    _insert(name).runTx(pool.getConnection())
  }

  private[repository] def _insert(name: String): ConnectionIO[User] = ConnectionIO { conn =>
    val id = UUID.randomUUID().toString
    val query =
      """insert into Users(`id`, `name`, `created_at`) values (?, ?, ?)"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, id)
    stmt.setString(2, name)
    stmt.setTimestamp(3, Timestamp.from(clock.instant()))
    stmt.executeQuery(query)
    User(id, name)
  }

  override def fetchBy(userId: String): Option[User] = {
    _fetchBy(userId).runReadOnly(pool.getConnection()).toOption.flatten
  }

  private[repository] def _fetchBy(userId: String): ConnectionIO[Option[User]] = ConnectionIO { conn =>
    val query = """select * from Users where user_id = ?"""
    val mapping: ResultSet => User = row =>
      User(
        id = row.getString("id"),
        name = row.getString("name")
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, userId)
    select(stmt, mapping)(conn)
  }

}
