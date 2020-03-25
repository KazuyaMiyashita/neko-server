package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, CONFLICT, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}

import neko.chat.application.entity.Token
import neko.chat.application.usecase.{FetchUserIdByToken, CreateUser, EditUserInfo}

class UserController(
    fetchUserIdByToken: FetchUserIdByToken,
    createUser: CreateUser,
    editUserInfo: EditUserInfo,
    cc: ControllerComponent
) {

  import UserController._

  def create(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      createUserRequest <- parseJsonRequest(request, createUserRequestDecoder)
      _ <- createUser.execute(createUserRequest).left.map {
        case errors: CreateUser.Error.ValidateErrors =>
          createJsonResponse(BAD_REQUEST, errors)(createUserValidateErrorsEncoder)
        case CreateUser.Error.DuplicateEmail => cc.responseBuilder.build(CONFLICT, "メールアドレスが既に登録されています")
        case CreateUser.Error.Unknown(e) => {
          println(e)
          cc.responseBuilder.build(INTERNAL_SERVER_ERROR)
        }
      }
    } yield cc.responseBuilder.build(OK)
    result.merge
  }

  def edit(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(cc.responseBuilder.build(UNAUTHORIZED))
      newUserName <- parseJsonRequest(request, nameDecoder)
      userId      <- fetchUserIdByToken.execute(token).toRight(cc.responseBuilder.build(UNAUTHORIZED))
      _ <- editUserInfo.execute(EditUserInfo.Request(userId, newUserName)).left.map {
        case EditUserInfo.Error.UserNameTooLong => cc.responseBuilder.build(BAD_REQUEST, "ユーザー名は20文字以下である必要があります")
      }
    } yield cc.responseBuilder.build(OK)
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

object UserController {

  val createUserRequestDecoder: JsonDecoder[CreateUser.Request] = new JsonDecoder[CreateUser.Request] {
    override def decode(js: JsValue): Option[CreateUser.Request] = {
      for {
        name        <- (js \ "name").as[String]
        loginName   <- (js \ "email").as[String]
        rawPassword <- (js \ "password").as[String]
      } yield CreateUser.Request(name, loginName, rawPassword)
    }
  }

  val createUserValidateErrorsEncoder: JsonEncoder[CreateUser.Error.ValidateErrors] = {
    new JsonEncoder[CreateUser.Error.ValidateErrors] {
      override def encode(value: CreateUser.Error.ValidateErrors): JsValue = {
        val errorsMap: Map[String, String] = {
          value.errors.map {
            case CreateUser.ValidateError.UserNameTooLong     => "name"     -> "ユーザー名は20文字以下である必要があります"
            case CreateUser.ValidateError.EmailWrongFormat    => "email"    -> "メールアドレスの形式がおかしい"
            case CreateUser.ValidateError.RawPasswordTooShort => "password" -> "パスワードは8文字以上である必要があります"
          }.toMap
        }
        Json.obj("errors" -> Json.encode(errorsMap))
      }
    }
  }

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

}
