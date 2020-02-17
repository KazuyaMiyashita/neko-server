package neko.chat.repository

import neko.chat.entity.User

trait UserRepository {

  def create(name: String): Either[Throwable, User]

}
