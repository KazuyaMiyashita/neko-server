package neko.chat.application.entity

import java.util.UUID
import java.time.Instant

case class Message(
    id: Message.MessageId,
    userId: User.UserId,
    body: Message.MessageBody,
    createdAt: Instant
)

object Message {

  case class MessageId(uuid: UUID) {
    def value: String = uuid.toString
  }
  case class MessageBody(value: String)
  object MessageBody {
    def of(body: String): Either[Error, MessageBody] = {
      if (body.length > 0 && body.length <= 100) Right(MessageBody(body))
      else Left(Error.TooLong)
    }

    sealed trait Error
    object Error {
      case object TooLong extends Error
    }
  }

}
