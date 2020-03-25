package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId
import neko.chat.application.usecase.{FetchUserIdByToken, Login, Logout}

class AuthController(
    fetchUserIdByToken: FetchUserIdByToken,
    login: Login,
    logout: Logout,
    cc: ControllerComponent
) {

  import AuthController._

  def session(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(cc.responseBuilder.build(UNAUTHORIZED))
      userId <- fetchUserIdByToken.execute(token).toRight(cc.responseBuilder.build(UNAUTHORIZED))
    } yield {
      createJsonResponse(OK, SessionResponse(userId))
    }
    result.merge
  }

  def login(request: HttpRequest): HttpResponse = {
    val result = for {
      loginRequest <- parseJsonRequest(request, loginRequestDecoder)
      token <- login.execute(loginRequest).left.map {
        case Login.Error.EmailWrongFormat    => cc.responseBuilder.build(BAD_REQUEST, "メールアドレスの形式がおかしい")
        case Login.Error.RawPasswordTooShort => cc.responseBuilder.build(BAD_REQUEST, "パスワードは8文字以上である必要があります")
        case Login.Error.UserNotExist        => cc.responseBuilder.build(UNAUTHORIZED, "メールアドレスかパスワードが間違っている")
        case Login.Error.Unknown(e) => {
          println(e)
          cc.responseBuilder.build(INTERNAL_SERVER_ERROR)
        }
      }
    } yield {
      cc.responseBuilder
        .withHeader("Set-Cookie" -> s"token=${token.value}; Path=/")
        .build(OK)
    }
    result.merge
  }

  def logout(request: HttpRequest): HttpResponse = {
    val result = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(cc.responseBuilder.build(OK))
    } yield {
      logout.execute(token)
      cc.responseBuilder
        .withHeader("Set-Cookie" -> "token=; Path=/; Max-Age=0")
        .build(OK)

    }
    result.merge
  }

  private def createJsonResponse[T](status: HttpStatus, result: T)(implicit encoder: JsonEncoder[T]): HttpResponse = {
    cc.responseBuilder
      .withContentType("application/json")
      .build(
        status = status,
        body = Json.format(Json.encode(result))
      )
  }

  private def parseJsonRequest[T](request: HttpRequest, decoder: JsonDecoder[T]): Either[HttpResponse, T] = {
    val badRequest = cc.responseBuilder.build(BAD_REQUEST, "json parse error")
    Json
      .parse(request.bodyAsString)
      .flatMap(decoder.decode)
      .toRight(badRequest)
  }

}

object AuthController {

  case class SessionResponse(userId: UserId)
  implicit val sessionResponseEncoder: JsonEncoder[SessionResponse] = new JsonEncoder[SessionResponse] {
    override def encode(response: SessionResponse): JsValue = Json.obj("userId" -> Json.str(response.userId.value))
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
