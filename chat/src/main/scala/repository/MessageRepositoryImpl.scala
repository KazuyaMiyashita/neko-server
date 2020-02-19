package neko.chat.repository

import java.time.Clock
import java.util.UUID
import java.sql.{ResultSet, Timestamp}

import neko.core.jdbc.{DBPool, ConnectionIO}
import neko.core.jdbc.query._
import neko.chat.entity.Message

class MessageRepositoryImpl(pool: DBPool, clock: Clock) extends MessageRepository {

  override def post(roomId: String, userId: String, message: String): Either[Throwable, Message] = {
    _post(roomId, userId, message).runTx(pool.getConnection())
  }

  override def fetchAllByRoomId(roomId: String): Either[Throwable, List[Message]] = {
    _fetchAllByRoomId(roomId).runTx(pool.getConnection())
  }

  def _post(roomId: String, userId: String, message: String): ConnectionIO[Message] = ConnectionIO { conn =>
    val id        = UUID.randomUUID().toString
    val createdAt = clock.instant()
    val query =
      """insert into Messages(id, room_id, user_id, message, created_at) values (?, ?, ?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, id)
    stmt.setString(2, roomId)
    stmt.setString(3, userId)
    stmt.setString(4, message)
    stmt.setTimestamp(5, Timestamp.from(createdAt))
    stmt.executeUpdate()
    Message(id, roomId, userId, message, createdAt)
  }

  def _fetchAllByRoomId(roomId: String): ConnectionIO[List[Message]] = ConnectionIO { conn =>
    val query = """select * from Messages where room_id = ?
                  |  order by created_at desc;""".stripMargin
    val mapping: ResultSet => Message = row =>
      Message(
        id = row.getString("id"),
        roomId = row.getString("room_id"),
        userId = row.getString("user_id"),
        message = row.getString("message"),
        createdAt = row.getTimestamp("created_at").toInstant
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, roomId)
    list(stmt, mapping)(conn)
  }

}
