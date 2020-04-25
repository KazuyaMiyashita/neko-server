package neko.chat.application.usecase

import java.time.Instant
import scala.util.{Success, Failure}
import neko.core.jdbc.ConnectionIORunner
import neko.chat.application.entity.{Message, User}
import neko.chat.application.repository.MessageRepository

class GetMessages(
    messageRepository: MessageRepository,
    connectionIORunner: ConnectionIORunner
) {

  def latest50messages(): Either[GetMessages.Error, List[GetMessages.MessageResponse]] = {
    connectionIORunner.runReadOnly(messageRepository.fetchLatest50messages()) match {
      case Failure(e) => Left(GetMessages.Error.Unknown(e))
      case Success(vs) => {
        val a = vs.merge
        Right(a.map { v =>
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
