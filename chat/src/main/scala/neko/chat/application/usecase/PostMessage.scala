package neko.chat.application.usecase

import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message
import neko.chat.application.entity.Message.MessageBody
import neko.chat.application.repository.MessageRepository

class PostMessage(messageRepository: MessageRepository) {

  def execute(request: PostMessage.Request): Either[PostMessage.Error, Message] = {
    for {
      messageBody <- request.validate
    } yield {
      val message = messageRepository.createMessageEntity(request.userId, messageBody)
      messageRepository.saveMessage(message)
      message
    }
  }

}

object PostMessage {
  case class Request(
      userId: UserId,
      body: String
  ) {
    def validate: Either[Error.ValidateError, MessageBody] = {
      for {
        b <- MessageBody.of(body).left.map {
          case MessageBody.Error.TooLong => Error.MessageBodyTooLong
        }
      } yield b
    }
  }

  sealed trait Error
  object Error {
    sealed trait ValidateError     extends Error
    case object MessageBodyTooLong extends ValidateError
  }
}
