package neko.chat.auth

import neko.chat.entity.User
import neko.core.http.{Request, Response, BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.core.jdbc.DBPool
import neko.chat.repository.AuthRepository

class AuthenticatorImpl(
    authRepository: AuthRepository,
    dbPool: DBPool
) extends Authenticator {

  def auth(request: Request): Either[Response, User] = {
    for {
      token <- request.header.getQueries
        .get("token")
        .map(Token.apply)
        .toRight(Response(BAD_REQUEST, "token required"))
      user <- authRepository
        .authenticate(token)
        .runReadOnly(dbPool.getConnection())
        .left
        .map(_ => Response(INTERNAL_SERVER_ERROR))
        .flatMap(_.toRight(Response(UNAUTHORIZED, "incorrect token")))
    } yield user
  }

}
