package neko.chat.application.entity

import java.util.UUID
import java.time.Instant

case class User(
    id: User.UserId,
    name: User.UserName,
    createdAt: Instant
)

object User {

  case class UserId(value: UUID) {
    def asString: String = value.toString
  }

  case class UserName(value: String)
  object UserName {
    def from(name: String): Either[UserName.Error, UserName] = {
      if (name.length > 0 && name.length <= 20) Right(UserName(name))
      else Left(Error.TooLong)
    }

    sealed trait Error
    object Error {
      case object TooLong extends Error
    }
  }

}
