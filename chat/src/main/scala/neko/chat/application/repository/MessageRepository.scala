package neko.chat.application.repository

import scala.util.Try

import neko.chat.application.entity.{Message, User}
import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message.MessageBody

trait MessageRepository {

  import MessageRepository._

  def fetchLatest50messages(): Try[List[MessageResponse]]
  def createMessageEntity(userId: UserId, body: MessageBody): Message
  def saveMessage(message: Message): Try[Unit]

}

object MessageRepository {

  case class MessageResponse(message: Message, user: User)

}
