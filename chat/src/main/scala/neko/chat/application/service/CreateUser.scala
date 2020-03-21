package neko.chat.application.service

import scala.util.{Success, Failure}

import neko.chat.application.entity.{User, Email, RawPassword}
import neko.chat.application.entity.User.UserName
import neko.chat.application.repository.UserRepository

trait CreateUser {
  import CreateUser._
  def execute(request: Request): Either[Error, User]
}

object CreateUser {
  case class Request(
      userName: String,
      email: String,
      rawPassword: String
  ) {
    def validate: Either[Error.ValidateError, (UserName, Email, RawPassword)] = {
      for {
        un <- UserName.from(userName).left.map {
          case UserName.Error.TooLong => Error.UserNameTooLong
        }
        e <- Email.from(email).left.map {
          case Email.Error.WrongFormat => Error.EmailWrongFormat
        }
        rp <- RawPassword.from(rawPassword).left.map {
          case RawPassword.Error.TooShort => Error.RawPasswordTooShort
        }
      } yield (un, e, rp)
    }
  }

  sealed trait Error
  object Error {
    sealed trait ValidateError       extends Error
    case object UserNameTooLong      extends ValidateError
    case object EmailWrongFormat     extends ValidateError
    case object RawPasswordTooShort  extends ValidateError
    case object DuplicateEmail       extends Error
    case class Unknown(e: Throwable) extends Error
  }
}

class CreateUserImpl(
    userRepository: UserRepository
) extends CreateUser {

  import CreateUser._

  override def execute(request: Request): Either[Error, User] = {
    for {
      t <- request.validate
      (userName, email, rawPassword) = (t._1, t._2, t._3)
      user <- userRepository.saveNewUser(userName, email, rawPassword) match {
        case Failure(e) => Left(Error.Unknown(e))
        case Success(v) =>
          v match {
            case Left(UserRepository.SaveNewUserError.DuplicateEmail(_)) => Left(Error.DuplicateEmail)
            case Right(user)                                             => Right(user)
          }
      }
    } yield user
  }

}
