package neko.chat.repository

import neko.chat.entity.User
import neko.chat.auth.Token
import neko.core.jdbc.ConnectionIO

trait AuthRepository {

  def authenticate(token: Token): ConnectionIO[Option[User]]

  def login(email: String, rawPassword: String): ConnectionIO[Option[Token]]

  def logout(token: Token): ConnectionIO[Unit]

}
