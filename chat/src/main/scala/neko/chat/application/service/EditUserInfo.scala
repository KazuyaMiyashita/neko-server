package neko.chat.application.service

import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.repository.UserRepository

trait EditUserInfo {
  import EditUserInfo._
  def execute(userId: UserId, newUserNameStr: String): Either[EditUserInfoError, Unit]
}

object EditUserInfo {
  sealed trait EditUserInfoError
  case class ValidateError(asString: String) extends EditUserInfoError
}

class EditUserInfoImpl(
    userRepository: UserRepository
) extends EditUserInfo {

  import EditUserInfo._

  def validate(newUserNameStr: String): Either[ValidateError, UserName] = {
    UserName.validate(newUserNameStr).left.map(ValidateError)
  }

  override def execute(userId: UserId, newUserNameStr: String): Either[EditUserInfoError, Unit] = {
    validate(newUserNameStr).map { newUserName =>
      userRepository.updateUserName(userId, newUserName)
    }
  }

}
