package neko.chat.application.repository

import neko.chat.application.entity.{Message, User}
import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message.MessageBody
import neko.core.jdbc.ConnectionIO

trait MessageRepository {

  import MessageRepository._

  def fetchLatest50messages(): List[MessageResponse]

  def createMessageEntity(userId: UserId, messageBody: MessageBody): Message
  def saveMessage(message: Message): ConnectionIO[Unit]

}

object MessageRepository {

  case class MessageResponse(message: Message, user: User)

}
