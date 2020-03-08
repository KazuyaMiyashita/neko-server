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
    def validate(body: String): Either[String, MessageBody] = {
      if (body.length > 0 && body.length <= 100) Right(MessageBody(body))
      else Left("メッセージは100文字以下である必要があります")
    }
  }

}
