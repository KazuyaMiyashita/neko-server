package neko.chat.application.entity

case class RawPassword(value: String) {
  require(value.length >= 8)
}

object RawPassword {
  def of(value: String): Either[Error, RawPassword] = {
    Either.cond(value.length >= 8, RawPassword(value), Error.TooShort)
  }

  sealed trait Error
  object Error {
    case object TooShort extends Error
  }
}
