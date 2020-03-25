package neko.chat.application.repository

import scala.util.Try

import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}

trait UserRepository {

  def saveNewUser(
      userName: UserName,
      email: Email,
      rawPassword: RawPassword
  ): Try[Either[UserRepository.SaveNewUserError, User]]

  def fetchUserBy(userId: UserId): Try[Option[User]]

  def fetchUserIdBy(email: Email, rawPassword: RawPassword): Try[Option[UserId]]

  def createHashedPassword(rawPassword: RawPassword): HashedPassword

}

object UserRepository {

  sealed trait SaveNewUserError
  object SaveNewUserError {
    case class DuplicateEmail(e: Throwable) extends SaveNewUserError
  }

}
