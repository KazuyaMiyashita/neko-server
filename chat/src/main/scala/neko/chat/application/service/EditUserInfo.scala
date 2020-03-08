package neko.chat.application.service

import neko.chat.application.entity.User
import neko.chat.application.entity.User.UserName
import neko.chat.application.repository.UserRepository

class EditUserInfo(userRepository: UserRepository) {

  import EditUserInfo._

  def validate(request: EditUserInfoRequest): Either[ValidateError, UserName] = {
    UserName.validate(request.newUserName).left.map(ValidateError)
  }

  def execute(user: User, request: EditUserInfoRequest): Either[ValidateError, User] = {
    validate(request).map { newUserName =>
      val editedUser = user.copy(name = newUserName)
      userRepository.update(editedUser)
      editedUser
    }
  }

}

object EditUserInfo {

  case class EditUserInfoRequest(newUserName: String)

  sealed trait EditUserInfoError
  case class ValidateError(asString: String) extends EditUserInfoError

}
