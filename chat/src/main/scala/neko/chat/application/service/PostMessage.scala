package neko.chat.application.service

import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message
import neko.chat.application.entity.Message.MessageBody
import neko.chat.application.repository.MessageRepository

class PostMessage(messageRepository: MessageRepository) {

  import PostMessage._

  def validate(request: PostMessageRequest): Either[ValidateError, (UserId, MessageBody)] = {
    val e: Either[String, MessageBody] = MessageBody.validate(request.body)
    e.left
      .map(ValidateError.apply)
      .map(body => (request.userId, body))
  }

  def execute(request: PostMessageRequest): Either[PostMessageError, Message] = {
    validate(request).map {
      case (userId, messageBody) =>
        val message = messageRepository.createMessageEntity(userId, messageBody)
        messageRepository.saveMessage(message)
        message
    }
  }

}

object PostMessage {

  case class PostMessageRequest(userId: UserId, body: String)

  trait PostMessageError
  case class ValidateError(asString: String) extends PostMessageError

}
