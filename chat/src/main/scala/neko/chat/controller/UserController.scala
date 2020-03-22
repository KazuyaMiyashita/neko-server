package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, CONFLICT, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}

import neko.chat.application.entity.Token
import neko.chat.application.service.{FetchUserIdByToken, CreateUser, EditUserInfo}
import neko.chat.application.service.CreateUser
import neko.chat.application.service.EditUserInfo

class UserController(
    fetchUserIdByToken: FetchUserIdByToken,
    createUser: CreateUser,
    editUserInfo: EditUserInfo
) {

  import UserController._

  def create(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      createUserRequest <- parseJsonRequest(request, createUserRequestDecoder)
      _ <- createUser.execute(createUserRequest).left.map {
        case CreateUser.Error.UserNameTooLong     => HttpResponse(BAD_REQUEST, "ユーザー名は20文字以下である必要があります")
        case CreateUser.Error.EmailWrongFormat    => HttpResponse(BAD_REQUEST, "メールアドレスの形式がおかしい")
        case CreateUser.Error.RawPasswordTooShort => HttpResponse(BAD_REQUEST, "パスワードは8文字以上である必要があります")
        case CreateUser.Error.DuplicateEmail      => HttpResponse(CONFLICT, "メールアドレスが既に登録されています")
        case CreateUser.Error.Unknown(e)          => {
          println(e)
          HttpResponse(INTERNAL_SERVER_ERROR)
        }
      }
    } yield HttpResponse(OK)
    result.merge
  }

  def edit(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(UNAUTHORIZED))
      newUserName <- parseJsonRequest(request, nameDecoder)
      userId      <- fetchUserIdByToken.execute(token).toRight(HttpResponse(UNAUTHORIZED))
      _ <- editUserInfo.execute(EditUserInfo.Request(userId, newUserName)).left.map {
        case EditUserInfo.Error.UserNameTooLong => HttpResponse(BAD_REQUEST, "ユーザー名は20文字以下である必要があります")
      }
    } yield HttpResponse(OK)
    result.merge
  }

}

object UserController {

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

  val createUserRequestDecoder: JsonDecoder[CreateUser.Request] = new JsonDecoder[CreateUser.Request] {
    override def decode(js: JsValue): Option[CreateUser.Request] = {
      for {
        name        <- (js \ "name").as[String]
        loginName   <- (js \ "email").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield CreateUser.Request(name, loginName, rawPassword)
    }
  }

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

}
