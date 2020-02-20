package neko.chat.auth

import neko.chat.entity.User
import neko.core.http.{Request, Response, BAD_REQUEST, INTERNAL_SERVER_ERROR}
import neko.core.jdbc.DBPool
import neko.chat.repository.AuthRepository

class Authenticator(
    authRepository: AuthRepository,
    dbPool: DBPool
) {

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
        .flatMap(_.toRight(Response(BAD_REQUEST, "incorrect token")))
    } yield user
  }

}
