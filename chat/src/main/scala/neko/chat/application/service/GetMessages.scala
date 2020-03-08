package neko.chat.application.service

import neko.chat.application.entity.{Message, User}
import neko.chat.application.repository.MessageRepository
import java.time.Instant

class GetMessages(messageRepository: MessageRepository) {

  import GetMessages._

  def latest50messages(): List[MessageResponse] = {
    messageRepository.fetchLatest50messages().map {
      case MessageRepository.MessageResponse(message, user) =>
        MessageResponse(message.id, message.body, user.name, message.createdAt)
    }
  }

}

object GetMessages {

  case class MessageResponse(
      id: Message.MessageId,
      body: Message.MessageBody,
      userName: User.UserName,
      createdAt: Instant
  )

}
