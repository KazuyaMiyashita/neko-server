package neko.chat.application.service

import scala.util.{Success, Failure}

import neko.chat.application.entity.{Email, Token, RawPassword}
import neko.chat.application.repository.{UserRepository, TokenRepository}

trait Login {
  def execute(request: Login.Request): Either[Login.Error, Token]
}

object Login {
  case class Request(
      email: String,
      rawPassword: String
  ) {
    def validate: Either[Error.ValidateError, (Email, RawPassword)] = {
      for {
        e <- Email.from(email).left.map {
          case Email.Error.WrongFormat => Error.EmailWrongFormat
        }
        rp <- RawPassword.from(rawPassword).left.map {
          case RawPassword.Error.TooShort => Error.RawPasswordTooShort
        }
      } yield (e, rp)
    }
  }

  sealed trait Error
  object Error {
    sealed trait ValidateError       extends Error
    case object EmailWrongFormat     extends ValidateError
    case object RawPasswordTooShort  extends ValidateError
    case object UserNotExist         extends Error
    case class Unknown(e: Throwable) extends Error
  }
}

class LoginImpl(
    userRepository: UserRepository,
    tokenRepositoty: TokenRepository
) extends Login {

  import Login._

  def execute(request: Request): Either[Error, Token] = {
    for {
      t <- request.validate
      (email, rawPassword) = (t._1, t._2)
      userId <- userRepository.fetchUserIdBy(email, rawPassword) match {
        case Failure(e) => Left(Error.Unknown(e))
        case Success(v) =>
          v match {
            case None         => Left(Error.UserNotExist)
            case Some(userId) => Right(userId)
          }
      }
    } yield {
      val token = tokenRepositoty.createToken(userId)
      tokenRepositoty.saveToken(userId, token)
      token
    }
  }

}
