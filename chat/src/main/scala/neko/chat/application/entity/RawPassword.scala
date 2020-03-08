package neko.chat.application.entity

case class RawPassword(value: String)

object RawPassword {
  def validate(value: String): Either[String, RawPassword] = {
    Either.cond(value.length >= 8, RawPassword(value), "パスワードは8文字以上である必要があります")
  }
}
