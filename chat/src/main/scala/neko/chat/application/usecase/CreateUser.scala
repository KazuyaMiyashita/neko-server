package neko.chat.application.usecase

import scala.util.{Success, Failure}

import neko.chat.application.entity.{User, Email, RawPassword}
import neko.chat.application.entity.User.UserName
import neko.chat.application.repository.UserRepository

class CreateUser(userRepository: UserRepository) {
  def execute(request: CreateUser.Request): Either[CreateUser.Error, User] = {
    for {
      t <- request.validate
      (userName, email, rawPassword) = (t._1, t._2, t._3)
      user <- userRepository.saveNewUser(userName, email, rawPassword) match {
        case Failure(e) => Left(CreateUser.Error.Unknown(e))
        case Success(v) =>
          v match {
            case Left(UserRepository.SaveNewUserError.DuplicateEmail(_)) => Left(CreateUser.Error.DuplicateEmail)
            case Right(user)                                             => Right(user)
          }
      }
    } yield user
  }

}

object CreateUser {
  case class Request(
      userName: String,
      email: String,
      rawPassword: String
  ) {
    def validate: Either[Error.ValidateErrors, (UserName, Email, RawPassword)] = {
      val _un: Either[ValidateError, UserName] = UserName.from(userName).left.map {
        case UserName.Error.TooLong => ValidateError.UserNameTooLong
      }
      val _e: Either[ValidateError, Email] = Email.from(email).left.map {
        case Email.Error.WrongFormat => ValidateError.EmailWrongFormat
      }
      val _rp: Either[ValidateError, RawPassword] = RawPassword.from(rawPassword).left.map {
        case RawPassword.Error.TooShort => ValidateError.RawPasswordTooShort
      }
      (_un, _e, _rp) match {
        case (Right(un), Right(e), Right(rp)) => Right((un, e, rp))
        case _ =>
          Left(Error.ValidateErrors(List(_un, _e, _rp).collect {
            case Left(v) => v
          }: _*))
      }
    }
  }

  sealed trait ValidateError
  object ValidateError {
    case object UserNameTooLong     extends ValidateError
    case object EmailWrongFormat    extends ValidateError
    case object RawPasswordTooShort extends ValidateError
  }
  sealed trait Error
  object Error {
    case class ValidateErrors(errors: ValidateError*) extends Error
    case object DuplicateEmail                        extends Error
    case class Unknown(e: Throwable)                  extends Error
  }
}
