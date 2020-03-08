package neko.chat.application.service

import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.UserName
import neko.chat.application.repository.UserRepository

class CreateUser(
    userRepository: UserRepository
) {

  import CreateUser._

  def validate(request: CreateUserRequest): Either[ValidateError, (UserName, Email, HashedPassword)] = {
    val e: Either[String, (UserName, Email, HashedPassword)] = for {
      userName    <- UserName.validate(request.userName)
      email       <- Email.validate(request.email)
      rawPassword <- RawPassword.validate(request.rawPassword)
    } yield {
      val hashedPassword = userRepository.createHashedPassword(rawPassword)
      (userName, email, hashedPassword)
    }
    e.left.map(ValidateError)
  }

  def execute(request: CreateUserRequest): Either[CreateUserError, User] = {
    validate(request).flatMap {
      case (userName, email, hashedPassword) =>
        userRepository.saveNewUser(userName, email, hashedPassword).left.map {
          case e: UserRepository.UserNotExistOrDuplicateUserNameException => DuplicateEmail(e)
        }
    }
  }

}

object CreateUser {

  case class CreateUserRequest(
      userName: String,
      email: String,
      rawPassword: String
  )

  sealed trait CreateUserError
  case class ValidateError(asString: String) extends CreateUserError
  case class DuplicateEmail(e: Throwable)    extends CreateUserError

}
