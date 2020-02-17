package neko.chat.repository

import java.time.Clock
import java.util.UUID
import java.sql.Timestamp
import java.sql.SQLException

import neko.jdbc.DBPool
import neko.chat.entity.User

class UserRepositoryImpl(pool: DBPool, clock: Clock) extends UserRepository {

  override def create(name: String): Either[Throwable, User] = {
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

}
