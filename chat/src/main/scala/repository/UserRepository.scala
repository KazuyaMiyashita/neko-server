package neko.chat.repository

import neko.chat.entity.User

trait UserRepository {

  def insert(name: String): Either[Throwable, User]

  def fetchBy(userId: String): Option[User]

}
