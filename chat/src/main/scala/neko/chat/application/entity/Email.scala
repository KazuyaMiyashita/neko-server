package neko.chat.application.entity

case class Email(value: String)

object Email {

  val re = "[^@]+@[^@]+".r
  def of(value: String): Either[Error, Email] = {
    Either.cond(re.matches(value), Email(value), Error.WrongFormat)
  }

  sealed trait Error
  object Error {
    case object WrongFormat extends Error
  }

}
