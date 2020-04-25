package neko.chat.application.repository

import neko.core.jdbc.ConnectionIO
import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}

trait UserRepository {

  def saveNewUser(
      userName: UserName,
      email: Email,
      rawPassword: RawPassword
  ): ConnectionIO[UserRepository.SaveNewUserError, User]

  def createHashedPassword(rawPassword: RawPassword): HashedPassword

  def fetchUserIdBy(email: Email, rawPassword: RawPassword): ConnectionIO[Nothing, Option[UserId]]

  def fetchBy(userId: UserId): ConnectionIO[Nothing, Option[User]]

  def updateUserName(userId: UserId, newUserName: UserName): ConnectionIO[Nothing, Unit]

}

object UserRepository {

  sealed trait SaveNewUserError
  object SaveNewUserError {
    case class DuplicateEmail(e: Throwable) extends SaveNewUserError
  }

}
