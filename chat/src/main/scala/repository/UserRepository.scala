package neko.chat.repository

import neko.core.jdbc.ConnectionIO
import neko.chat.entity.User
import java.util.UUID

trait UserRepository {

  def create(user: User): ConnectionIO[Unit]

  def fetchBy(userId: UUID): ConnectionIO[Option[User]]

}
