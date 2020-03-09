package neko.chat.application.repository

import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}

trait UserRepository {

  def saveNewUser(userName: UserName, email: Email, hashedPassword: HashedPassword): Either[Throwable, User]

  def createHashedPassword(rawPassword: RawPassword): HashedPassword

  def fetchUserIdBy(email: Email, hashedPassword: HashedPassword): Option[UserId]

  def fetchBy(userId: UserId): Option[User]

  def updateUserName(userId: UserId, newUserName: UserName): Unit

}

object UserRepository {

  class UserNotExistOrDuplicateUserNameException(e: Throwable) extends Exception(e)

}
