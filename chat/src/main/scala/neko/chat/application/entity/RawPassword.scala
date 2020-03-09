package neko.chat.application.entity

case class RawPassword(value: String) {
  require(value.length >= 8)
}

object RawPassword {
  def validate(value: String): Either[String, RawPassword] = {
    Either.cond(value.length >= 8, RawPassword(value), "パスワードは8文字以上である必要があります")
  }
}
