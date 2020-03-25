package neko.chat.application.usecase

import scala.util.{Success, Failure}

import neko.chat.application.entity.{Email, Token, RawPassword}
import neko.chat.application.repository.{UserRepository, TokenRepository}

class Login(
    userRepository: UserRepository,
    tokenRepositoty: TokenRepository
) {

  def execute(request: Login.Request): Either[Login.Error, Token] = {
    for {
      t <- request.validate
      (email, rawPassword) = (t._1, t._2)
      userId <- userRepository.fetchUserIdBy(email, rawPassword) match {
        case Failure(e) => Left(Login.Error.Unknown(e))
        case Success(v) =>
          v match {
            case None         => Left(Login.Error.UserNotExist)
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

object Login {
  case class Request(
      email: String,
      rawPassword: String
  ) {
    def validate: Either[Error.ValidateError, (Email, RawPassword)] = {
      for {
        e <- Email.of(email).left.map {
          case Email.Error.WrongFormat => Error.EmailWrongFormat
        }
        rp <- RawPassword.of(rawPassword).left.map {
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
