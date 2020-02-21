package neko.chat.repository

import neko.chat.entity.{Message, User}
import neko.core.jdbc.ConnectionIO

trait MessageRepository {

  import MessageRepository._

  def get(): ConnectionIO[List[MessageResponse]]

  def post(message: Message): ConnectionIO[Unit]

}

object MessageRepository {

  case class MessageResponse(message: Message, user: User)

}
