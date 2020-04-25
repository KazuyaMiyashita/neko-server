package neko.chat.application.usecase

import scala.util.{Success, Failure}
import neko.core.jdbc.ConnectionIORunner
import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Message
import neko.chat.application.entity.Message.MessageBody
import neko.chat.application.repository.MessageRepository

class PostMessage(
    messageRepository: MessageRepository,
    connectionIORunner: ConnectionIORunner
) {

  def execute(request: PostMessage.Request): Either[PostMessage.Error, Message] = {
    for {
      messageBody <- request.validate
      message = messageRepository.createMessageEntity(request.userId, messageBody)
      _ <- connectionIORunner.runTx(messageRepository.saveMessage(message)) match {
        case Failure(e) => Left(PostMessage.Error.Unknown(e))
        case Success(_) => Right(())
      }
    } yield message
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
    sealed trait ValidateError       extends Error
    case object MessageBodyTooLong   extends ValidateError
    case class Unknown(e: Throwable) extends Error
  }
}
