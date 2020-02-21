package neko.chat.repository

import java.util.UUID
import java.sql.{ResultSet, Timestamp}

import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._
import neko.chat.entity.{User, Message}
import neko.chat.repository.MessageRepository.MessageResponse

class MessageRepositoryImpl extends MessageRepository {

  override def get(): ConnectionIO[List[MessageResponse]] = ConnectionIO { conn =>
    val query =
      """select * from messages as m
        |  inner join users u
        |    where m.user_id = u.id
        |  order by m.created_at desc
        |  limit 50;
        |""".stripMargin

    val pstmt = conn.prepareStatement(query)
    val mapping: ResultSet => MessageResponse = row =>
      MessageResponse(
        message = Message(
          id = UUID.fromString(row.getString("m.id")),
          userId = UUID.fromString(row.getString("m.user_id")),
          body = row.getString("m.body"),
          createdAt = row.getTimestamp("m.created_at").toInstant
        ),
        user = User(
          id = UUID.fromString(row.getString("u.id")),
          screenName = row.getString("u.screen_name"),
          createdAt = row.getTimestamp("u.created_at").toInstant
        )
      )
    list(pstmt, mapping)(conn)
  }

  override def post(message: Message): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into messages(id, user_id, body, created_at) values (?, ?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, message.id.toString)
    stmt.setString(2, message.userId.toString)
    stmt.setString(3, message.body)
    stmt.setTimestamp(4, Timestamp.from(message.createdAt))
    stmt.executeUpdate()
  }

}
