package neko.chat.repository

import neko.chat.entity.User
import neko.chat.auth.Token
import neko.core.jdbc.ConnectionIO

trait AuthRepository {

  def authenticate(token: Token): ConnectionIO[Option[User]]

}
