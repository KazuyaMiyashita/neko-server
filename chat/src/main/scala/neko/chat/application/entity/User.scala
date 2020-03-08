package neko.chat.application.entity

import java.util.UUID
import java.time.Instant

case class User(
    id: User.UserId,
    name: User.UserName,
    createdAt: Instant
)

object User {

  case class UserId(value: UUID)
  case class UserName(value: String)
  object UserName {
    def validate(name: String): Either[String, UserName] = {
      if (name.length > 0 && name.length <= 100) Right(UserName(name))
      else Left("名前はは20文字以下である必要があります")
    }
  }

}
