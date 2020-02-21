package neko.chat.repository

import java.util.UUID
import java.time.Instant

import neko.chat.entity.Message
import neko.core.jdbc.ConnectionIO

trait MessageRepository {

  import MessageRepository._

  def post(message: Message): ConnectionIO[Unit]

  def get(): ConnectionIO[List[MessageResponse]]

}

object MessageRepository {

  case class MessageResponse(
      id: UUID,
      user: ResponseMessageUser,
      message: String,
      createdAt: Instant
  )

  case class ResponseMessageUser(
      id: UUID,
      screenName: String,
  )

}
