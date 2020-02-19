package neko.chat.repository

import java.util.UUID
import java.sql.{ResultSet, Timestamp}

import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._
import neko.chat.entity.User

class UserRepositoryImpl extends UserRepository {

  override def create(user: User): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into Users(id, name, created_at) values (?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, user.id.toString)
    stmt.setString(2, user.name)
    stmt.setTimestamp(3, Timestamp.from(user.createdAt))
    stmt.executeUpdate()
    ()
  }

  override def fetchBy(userId: UUID): ConnectionIO[Option[User]] = ConnectionIO { conn =>
    val query = """select * from Users where id = ?"""
    val mapping: ResultSet => User = row =>
      User(
        id = UUID.fromString(row.getString("id")),
        name = row.getString("name"),
        createdAt = row.getTimestamp("created_at").toInstant
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, userId.toString)
    select(stmt, mapping)(conn)
  }

}
