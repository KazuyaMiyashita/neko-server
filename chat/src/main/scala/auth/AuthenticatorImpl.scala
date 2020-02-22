package neko.chat.auth

import neko.chat.entity.User
import neko.core.http.{HttpRequest, HttpResponse, BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.core.jdbc.DBPool
import neko.chat.repository.AuthRepository

class AuthenticatorImpl(
    authRepository: AuthRepository,
    dbPool: DBPool
) extends Authenticator {

  def auth(request: HttpRequest): Either[HttpResponse, User] = {
    for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(BAD_REQUEST, "token required"))
      user <- authRepository
        .authenticate(token)
        .runReadOnly(dbPool.getConnection())
        .left
        .map { e =>
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
        .flatMap(_.toRight(HttpResponse(UNAUTHORIZED, "incorrect token")))
    } yield user
  }

}
