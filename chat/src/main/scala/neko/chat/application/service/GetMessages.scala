package neko.chat.application.service

import neko.chat.application.entity.{Message, User}
import neko.chat.application.repository.MessageRepository
import java.time.Instant
import scala.util.Failure
import scala.util.Success

trait GetMessages {
  import GetMessages._
  def latest50messages(): Either[Error, List[MessageResponse]]
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

class GetMessagesImpl(messageRepository: MessageRepository) extends GetMessages {

  import GetMessages._

  override def latest50messages(): Either[Error, List[MessageResponse]] = {
    messageRepository.fetchLatest50messages() match {
      case Failure(e) => Left(Error.Unknown(e))
      case Success(vs) => {
        Right(vs.map { v =>
          val MessageRepository.MessageResponse(message, user) = v
          MessageResponse(message.id, message.body, user.name, message.createdAt)
        })
      }
    }
  }

}
