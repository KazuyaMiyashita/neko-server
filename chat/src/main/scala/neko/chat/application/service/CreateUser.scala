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
    def validate: Either[Error.ValidateErrors, (UserName, Email, RawPassword)] = {
      val _un = UserName.from(userName).left.map {
        case UserName.Error.TooLong => Error.UserNameTooLong
      }
      val _e = Email.from(email).left.map {
        case Email.Error.WrongFormat => Error.EmailWrongFormat
      }
      val _rp = RawPassword.from(rawPassword).left.map {
        case RawPassword.Error.TooShort => Error.RawPasswordTooShort
      }
      val a = List(_un, _e, _rp).foldLeft(List.empty[Error.ValidateError]) { case (acc, a) =>
        a match {
          case Left(e) => e :: acc
          case _ => acc
        }
      }
      a match {
        case Nil => Right((_un.right.get, _e.right.get, _rp.right.get))
        case errs => Left(Error.ValidateErrors(errs))
      }
    }
  }

  sealed trait Error
  object Error {
    sealed trait ValidateError
    case object UserNameTooLong      extends ValidateError
    case object EmailWrongFormat     extends ValidateError
    case object RawPasswordTooShort  extends ValidateError
    case class ValidateErrors(errors: List[ValidateError]) extends Error
    case object DuplicateEmail       extends Error
    case class Unknown(e: Throwable) extends Error
  }
}

class CreateUserImpl(
    userRepository: UserRepository
) extends CreateUser {

  override def execute(request: CreateUser.Request): Either[CreateUser.Error, User] = {
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
