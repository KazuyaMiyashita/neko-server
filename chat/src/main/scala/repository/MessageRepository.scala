package neko.chat.repository

import java.util.UUID
import java.time.Instant

import neko.core.jdbc.ConnectionIO

trait MessageRepository {

  import MessageRepository._

  def post(message: PostMessage): ConnectionIO[Unit]

  def fetch(limit: Int): ConnectionIO[List[MessageResponse]]

}

object MessageRepository {

  case class PostMessage(
      id: UUID,
      roomId: UUID,
      userId: UUID,
      message: String,
      createdAt: Instant
  )

  case class MessageResponse(
      id: UUID,
      roomId: UUID,
      user: MessageUser,
      message: String,
      createdAt: Instant
  )

  case class MessageUser(
        id: UUID,
    screenName: String,
  )

}
