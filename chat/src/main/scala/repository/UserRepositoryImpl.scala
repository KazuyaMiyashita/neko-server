package neko.chat.repository

import java.time.Clock
import java.util.UUID
import java.sql.Timestamp
import java.sql.SQLException

import neko.core.jdbc.DBPool
import neko.chat.entity.User
import java.sql.ResultSet

class UserRepositoryImpl(pool: DBPool, clock: Clock) extends UserRepository {

  override def insert(name: String): Either[Throwable, User] = {
    val id = UUID.randomUUID().toString
    val query =
      """insert into Users(`id`, `name`, `created_at`) values (?, ?, ?)"""
    val conn = pool.getConnection()
    conn.setAutoCommit(false)
    try {
      val stmt = conn.prepareStatement(query)
      stmt.setString(1, id)
      stmt.setString(2, name)
      stmt.setTimestamp(3, Timestamp.from(clock.instant()))
      stmt.executeQuery(query)
      conn.commit()
      Right(User(id, name))
    } catch {
      case e: SQLException => {
        conn.rollback()
        e.printStackTrace()
        Left(e)
      }
    } finally {
      conn.close()
    }

  }

  override def fetchBy(userId: String): Option[User] = {
    val query = """select * from Users where user_id = ?"""
    val mapping: ResultSet => User = row =>
      User(
        id = row.getString("id"),
        name = row.getString("name")
      )
    val conn = pool.getConnection()
    try {
      val stmt = conn.prepareStatement(query)
      stmt.setString(1, userId)
      val resultSet = stmt.executeQuery(query)
      val userOpt: Option[User] = Iterator.continually(resultSet).takeWhile(_.next()).map(mapping).toList.headOption
      userOpt
    } catch {
      case e: SQLException => {
        e.printStackTrace()
        None
      }
    } finally {
      conn.close()
    }
  }

}
