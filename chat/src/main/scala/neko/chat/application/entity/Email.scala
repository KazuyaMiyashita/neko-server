package neko.chat.application.entity

case class Email(value: String)

object Email {

  val re = "[^@]+@[^@]+".r
  def validate(value: String): Either[String, Email] = {
    Either.cond(re.matches(value), Email(value), "emailの形式がおかしい")
  }

}
