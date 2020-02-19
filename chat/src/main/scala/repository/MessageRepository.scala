package neko.chat.repository

import neko.core.jdbc.ConnectionIO
import neko.chat.entity.Message
import java.util.UUID

trait MessageRepository {

  def post(message: Message): ConnectionIO[Unit]

  def fetchAllByRoomId(roomId: UUID): ConnectionIO[List[Message]]

}
