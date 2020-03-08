package neko.chat.application.repository

import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}
import java.util.UUID

trait UserRepository {

  def saveNewUser(userName: UserName, email: Email, hashedPassword: HashedPassword): Either[Throwable, User]

  def createHashedPassword(rawPassword: RawPassword): HashedPassword

  def fetchUserIdBy(email: Email, hashedPassword: HashedPassword): Option[UserId]

  def fetchBy(userId: UUID): Option[User]

  def update(user: User): Unit

}

object UserRepository {

  class UserNotExistOrDuplicateUserNameException(e: Throwable) extends Exception(e)

}
