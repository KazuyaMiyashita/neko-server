package neko.chat.repository

import java.util.UUID
import java.sql.{ResultSet, Timestamp}

import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._
import neko.chat.entity.Message

class MessageRepositoryImpl extends MessageRepository {

  override def post(message: Message): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into Messages(id, room_id, user_id, message, created_at) values (?, ?, ?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, message.id.toString)
    stmt.setString(2, message.roomId.toString)
    stmt.setString(3, message.userId.toString)
    stmt.setString(4, message.message)
    stmt.setTimestamp(5, Timestamp.from(message.createdAt))
    stmt.executeUpdate()
  }

  override def fetchAllByRoomId(roomId: UUID): ConnectionIO[List[Message]] = ConnectionIO { conn =>
    val query =
      """select * from Messages where room_id = ?
        |  order by created_at desc;""".stripMargin

    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, roomId.toString)

    val mapping: ResultSet => Message = row =>
      Message(
        id = UUID.fromString(row.getString("id")),
        roomId = UUID.fromString(row.getString("room_id")),
        userId = UUID.fromString(row.getString("user_id")),
        message = row.getString("message"),
        createdAt = row.getTimestamp("created_at").toInstant
      )
    list(pstmt, mapping)(conn)
  }

}
