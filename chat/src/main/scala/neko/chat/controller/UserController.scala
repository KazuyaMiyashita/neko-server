package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, CONFLICT, UNAUTHORIZED}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}

import neko.chat.application.entity.Token
import neko.chat.application.service.{FetchUserIdByToken, CreateUser, EditUserInfo}
import neko.chat.application.service.CreateUser.{CreateUserRequest, DuplicateEmail}
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
        case CreateUser.ValidateError(message) => HttpResponse(BAD_REQUEST, message)
        case DuplicateEmail(_)                 => HttpResponse(CONFLICT, "メールアドレスが既に登録されています")
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
      _ <- editUserInfo.execute(userId, newUserName).left.map {
        case EditUserInfo.ValidateError(message) => HttpResponse(BAD_REQUEST, message)
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

  val createUserRequestDecoder: JsonDecoder[CreateUserRequest] = new JsonDecoder[CreateUserRequest] {
    override def decode(js: JsValue): Option[CreateUserRequest] = {
      for {
        name        <- (js \ "name").as[String]
        loginName   <- (js \ "email").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield CreateUserRequest(name, loginName, rawPassword)
    }
  }

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

}
