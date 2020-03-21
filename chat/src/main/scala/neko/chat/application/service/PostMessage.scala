package neko.chat.application.service

import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message
import neko.chat.application.entity.Message.MessageBody
import neko.chat.application.repository.MessageRepository

trait PostMessage {
  def execute(request: PostMessage.Request): Either[PostMessage.Error, Message]
}

object PostMessage {
  case class Request(
      userId: UserId,
      body: String
  ) {
    def validate: Either[Error.ValidateError, MessageBody] = {
      for {
        b <- MessageBody.from(body).left.map {
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

class PostMessageImpl(messageRepository: MessageRepository) extends PostMessage {

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
