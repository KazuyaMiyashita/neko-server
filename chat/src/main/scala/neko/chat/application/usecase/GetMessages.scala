package neko.chat.application.usecase

import neko.chat.application.entity.{Message, User}
import neko.chat.application.repository.MessageRepository
import java.time.Instant
import scala.util.Failure
import scala.util.Success

class GetMessages(messageRepository: MessageRepository) {

  def latest50messages(): Either[GetMessages.Error, List[GetMessages.MessageResponse]] = {
    messageRepository.fetchLatest50messages() match {
      case Failure(e) => Left(GetMessages.Error.Unknown(e))
      case Success(vs) => {
        Right(vs.map { v =>
          val MessageRepository.MessageResponse(message, user) = v
          GetMessages.MessageResponse(message.id, message.body, user.name, message.createdAt)
        })
      }
    }
  }

}

object GetMessages {
  sealed trait Error
  object Error {
    case class Unknown(e: Throwable) extends Error
  }

  case class MessageResponse(
      id: Message.MessageId,
      body: Message.MessageBody,
      userName: User.UserName,
      createdAt: Instant
  )
}
