package neko.chat.application.service

import neko.chat.application.entity.{Email, Token, RawPassword, HashedPassword}
import neko.chat.application.repository.{UserRepository, TokenRepository}

trait Login {
  import Login._
  def execute(request: LoginRequest): Either[LoginError, Token]
}

object Login {
  case class LoginRequest(
      email: String,
      rawPassword: String
  )
  sealed trait LoginError
  case class ValidateError(asString: String) extends LoginError
  object UserNotExist                        extends LoginError
}

class LoginImpl(
    userRepository: UserRepository,
    tokenRepositoty: TokenRepository
) extends Login {

  import Login._

  def validate(request: LoginRequest): Either[ValidateError, (Email, HashedPassword)] = {
    val e: Either[String, (Email, HashedPassword)] = for {
      email <- Email.validate(request.email)
    } yield {
      val hashedPassword = userRepository.createHashedPassword(RawPassword(request.rawPassword))
      (email, hashedPassword)
    }
    e.left.map(ValidateError)
  }

  override def execute(request: LoginRequest): Either[LoginError, Token] = {
    validate(request).flatMap {
      case (email, hashedPassword) =>
        userRepository
          .fetchUserIdBy(email, hashedPassword)
          .toRight(UserNotExist)
          .map { userId =>
            val token = tokenRepositoty.createToken(userId)
            tokenRepositoty.saveToken(userId, token)
            token
          }
    }
  }

}
