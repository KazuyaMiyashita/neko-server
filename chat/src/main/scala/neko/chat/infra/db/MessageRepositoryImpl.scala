package neko.chat.infra.db

import java.util.UUID
import java.time.Clock
import java.sql.{ResultSet, Timestamp}

import scala.util.Try

import neko.core.jdbc.{ConnectionIO, DBPool}
import neko.core.jdbc.query._

import neko.chat.application.entity.{User, Message}
import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.entity.Message.{MessageId, MessageBody}
import neko.chat.application.repository.MessageRepository
import neko.chat.application.repository.MessageRepository.MessageResponse
import neko.core.jdbc.DBPool

class MessageRepositoryImpl(
    dbPool: DBPool,
    clock: Clock
) extends MessageRepository {

  import MessageRepositoryImpl._

  override def fetchLatest50messages(): Try[List[MessageResponse]] = {
    fetchLatest50messagesIO
      .runReadOnly(dbPool.getConnection())
      .map(_.merge)
  }

  override def createMessageEntity(userId: UserId, body: MessageBody): Message = {
    Message(
      id = MessageId(UUID.randomUUID()),
      userId = userId,
      body = body,
      createdAt = clock.instant()
    )
  }

  override def saveMessage(message: Message): Try[Unit] = {
    saveMessageIO(message)
      .runTx(dbPool.getConnection())
      .map(_.merge)
  }

}

object MessageRepositoryImpl {

  def fetchLatest50messagesIO(): ConnectionIO[Nothing, List[MessageResponse]] = ConnectionIO.right { conn =>
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
          id = MessageId(UUID.fromString(row.getString("m.id"))),
          userId = UserId(UUID.fromString(row.getString("m.user_id"))),
          body = MessageBody(row.getString("m.body")),
          createdAt = row.getTimestamp("m.created_at").toInstant
        ),
        user = User(
          id = UserId(UUID.fromString(row.getString("u.id"))),
          name = UserName(row.getString("u.name")),
          createdAt = row.getTimestamp("u.created_at").toInstant
        )
      )
    list(pstmt, mapping)(conn)
  }

  def saveMessageIO(message: Message): ConnectionIO[Nothing, Unit] = ConnectionIO.right { conn =>
    val query =
      """insert into messages(id, user_id, body, created_at) values (?, ?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, message.id.value)
    stmt.setString(2, message.userId.value)
    stmt.setString(3, message.body.value)
    stmt.setTimestamp(4, Timestamp.from(message.createdAt))
    stmt.executeUpdate()
  }

}
