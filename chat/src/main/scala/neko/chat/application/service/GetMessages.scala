package neko.chat.application.service

import neko.chat.application.entity.{Message, User}
import neko.chat.application.repository.MessageRepository
import java.time.Instant

trait GetMessages {
  import GetMessages._
  def latest50messages(): List[MessageResponse]
}

object GetMessages {
  case class MessageResponse(
      id: Message.MessageId,
      body: Message.MessageBody,
      userName: User.UserName,
      createdAt: Instant
  )
}

class GetMessagesImpl(messageRepository: MessageRepository) extends GetMessages {

  import GetMessages._

  override def latest50messages(): List[MessageResponse] = {
    messageRepository.fetchLatest50messages().map {
      case MessageRepository.MessageResponse(message, user) =>
        MessageResponse(message.id, message.body, user.name, message.createdAt)
    }
  }

}
