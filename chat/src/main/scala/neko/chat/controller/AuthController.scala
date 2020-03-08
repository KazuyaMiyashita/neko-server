package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.json.{Json, JsValue, JsonDecoder}
import neko.chat.auth.Token
import neko.chat.repository.AuthRepository
import neko.core.http.BAD_REQUEST
import neko.core.jdbc.DBPool
import neko.core.http.{OK, UNAUTHORIZED, INTERNAL_SERVER_ERROR}

class AuthController(
    authRepsoitory: AuthRepository,
    dbPool: DBPool
) {

  import AuthController._

  def login(request: HttpRequest): HttpResponse = {
    val result = for {
      a <- Json
        .parse(request.body.asString)
        .flatMap(loginRequestDecoder.decode)
        .toRight(HttpResponse(BAD_REQUEST, "リクエストの形式がおかしい"))
      LoginRequest(loginName, rawPassword) = a
      token <- authRepsoitory
        .login(loginName, rawPassword)
        .runTx(dbPool.getConnection())
        .left
        .map { e =>
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
        .flatMap(_.toRight(HttpResponse(UNAUTHORIZED, "メールアドレスかパスワードが間違っている")))
    } yield {
      HttpResponse(OK)
        .withContentType("application/json")
        .withHeader("Set-Cookie", s"token=${token.value}; Path=/")
    }
    result.merge
  }

  def logout(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(BAD_REQUEST, "token required"))
      _ <- authRepsoitory
        .logout(token)
        .runTx(dbPool.getConnection())
        .left
        .map { e =>
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
    } yield HttpResponse(OK)
    result.merge
  }

  def session(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(UNAUTHORIZED))
      userOpt <- authRepsoitory
        .authenticate(token)
        .runTx(dbPool.getConnection())
        .left
        .map { e =>
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
    } yield {
      userOpt match {
        case None    => HttpResponse(UNAUTHORIZED)
        case Some(_) => HttpResponse(OK)
      }
    }
    result.merge
  }

}

object AuthController {

  case class LoginRequest(loginName: String, rawPassword: String)
  val loginRequestDecoder: JsonDecoder[LoginRequest] = new JsonDecoder[LoginRequest] {
    override def decode(js: JsValue): Option[LoginRequest] = {
      for {
        loginName   <- (js \ "loginName").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield LoginRequest(loginName, rawPassword)
    }
  }

}
