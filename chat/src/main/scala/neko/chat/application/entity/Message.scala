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

  case class MessageId(value: UUID) {
    def asString: String = value.toString
  }
  case class MessageBody(value: String)
  object MessageBody {
    def from(body: String): Either[Error, MessageBody] = {
      if (body.length > 0 && body.length <= 100) Right(MessageBody(body))
      else Left(Error.TooLong)
    }

    sealed trait Error
    object Error {
      case object TooLong extends Error
    }
  }

}
