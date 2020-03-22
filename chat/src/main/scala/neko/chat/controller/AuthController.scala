package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId
import neko.chat.application.service.{FetchUserIdByToken, Login, Logout}
import neko.chat.application.service.Login

class AuthController(
    fetchUserIdByToken: FetchUserIdByToken,
    login: Login,
    logout: Logout
) {

  import AuthController._

  def session(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(UNAUTHORIZED))
      userId <- fetchUserIdByToken.execute(token).toRight(HttpResponse(UNAUTHORIZED))
    } yield {
      createJsonResponse(OK, SessionResponse(userId))
    }
    result.merge
  }

  def login(request: HttpRequest): HttpResponse = {
    val result = for {
      loginRequest <- parseJsonRequest(request, loginRequestDecoder)
      token <- login.execute(loginRequest).left.map {
        case Login.Error.EmailWrongFormat    => HttpResponse(BAD_REQUEST, "メールアドレスの形式がおかしい")
        case Login.Error.RawPasswordTooShort => HttpResponse(BAD_REQUEST, "パスワードは8文字以上である必要があります")
        case Login.Error.UserNotExist        => HttpResponse(UNAUTHORIZED, "メールアドレスかパスワードが間違っている")
        case Login.Error.Unknown(e) => {
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
      }
    } yield {
      HttpResponse(OK)
        .withHeader("Set-Cookie", s"token=${token.value}; Path=/")
    }
    result.merge
  }

  def logout(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(OK))
    } yield {
      logout.execute(token)
      HttpResponse(OK)
        .withHeader("Set-Cookie", "token=; Path=/; Max-Age=0")
    }
    result.merge
  }

}

object AuthController {

  def createJsonResponse[T](status: HttpStatus, result: T)(implicit encoder: JsonEncoder[T]): HttpResponse = {
    HttpResponse(status, Json.format(Json.encode(result)))
      .withContentType("application/json")
  }

  def parseJsonRequest[T](request: HttpRequest, decoder: JsonDecoder[T]): Either[HttpResponse, T] = {
    Json
      .parse(request.body.asString)
      .flatMap(decoder.decode)
      .toRight(HttpResponse(BAD_REQUEST, "json parse error"))
  }

  case class SessionResponse(userId: UserId)
  implicit val sessionResponseEncoder: JsonEncoder[SessionResponse] = new JsonEncoder[SessionResponse] {
    override def encode(value: SessionResponse): JsValue = Json.obj("userId" -> Json.str(value.userId.asString))
  }

  val loginRequestDecoder: JsonDecoder[Login.Request] = new JsonDecoder[Login.Request] {
    override def decode(js: JsValue): Option[Login.Request] = {
      for {
        email       <- (js \ "email").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield Login.Request(email, rawPassword)
    }
  }

}
