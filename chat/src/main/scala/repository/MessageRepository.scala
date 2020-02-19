package neko.chat.repository

import neko.chat.entity.Message

trait MessageRepository {

  def post(roomId: String, userId: String, message: String): Either[Throwable, Message]

  def fetchAllByRoomId(roomId: String): Either[Throwable, List[Message]]

}
