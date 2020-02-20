package neko.chat.controller

import neko.core.http.{Request, Response}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}
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

  def login(request: Request): Response = {
    val result = for {
      a <- Json
        .parse(request.body)
        .flatMap(loginRequestDecoder.decode)
        .toRight(Response(BAD_REQUEST, "リクエストの形式がおかしい"))
      LoginRequest(email, rawPassword) = a
      token <- authRepsoitory
        .login(email, rawPassword)
        .runReadOnly(dbPool.getConnection())
        .left
        .map { _ =>
          Response(INTERNAL_SERVER_ERROR)
        }
        .flatMap(_.toRight(Response(UNAUTHORIZED, "メールアドレスかパスワードが間違っている")))
    } yield {
      val jsonString = Json.format(tokenEncoder.encode(token))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

  def logout(request: Request): Response = {
    val result = for {
      token <- request.header.getQueries
        .get("token")
        .map(Token.apply)
        .toRight(Response(BAD_REQUEST, "token required"))
      _ <- authRepsoitory
        .logout(token)
        .runReadOnly(dbPool.getConnection())
        .left
        .map { _ =>
          Response(INTERNAL_SERVER_ERROR)
        }
    } yield Response(OK)
    result.merge
  }

}

object AuthController {

  case class LoginRequest(email: String, rawPassword: String)
  val loginRequestDecoder: JsonDecoder[LoginRequest] = new JsonDecoder[LoginRequest] {
    override def decode(js: JsValue): Option[LoginRequest] = {
      for {
        email       <- (js \ "email").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield LoginRequest(email, rawPassword)
    }
  }

  val tokenEncoder: JsonEncoder[Token] = new JsonEncoder[Token] {
    override def encode(token: Token): JsValue = {
      Json.obj("token" -> Json.str(token.value))
    }
  }

}