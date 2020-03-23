package neko.chat.application.usecase

import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.repository.UserRepository

trait EditUserInfo {
  def execute(request: EditUserInfo.Request): Either[EditUserInfo.Error, Unit]
}

object EditUserInfo {
  case class Request(
      userId: UserId,
      newUserName: String
  ) {
    def validate: Either[Error.ValidateError, UserName] = {
      for {
        nun <- UserName.from(newUserName).left.map {
          case UserName.Error.TooLong => Error.UserNameTooLong
        }
      } yield nun
    }
  }

  sealed trait Error
  object Error {
    sealed trait ValidateError  extends Error
    case object UserNameTooLong extends ValidateError
  }
}

class EditUserInfoImpl(
    userRepository: UserRepository
) extends EditUserInfo {

  override def execute(request: EditUserInfo.Request): Either[EditUserInfo.Error, Unit] = {
    for {
      newUserName <- request.validate
    } yield {
      userRepository.updateUserName(request.userId, newUserName)
      ()
    }
  }

}
