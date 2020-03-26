package neko.chat.controller

import neko.core.http._
import neko.core.json._

import neko.chat.application.usecase.CreateUser

class UserController(
    createUser: CreateUser,
    cc: ControllerComponent
) {

  import UserController._

  def create(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      createUserRequest <- parseJsonRequest(request, createUserRequestDecoder)
      _ <- createUser.execute(createUserRequest).left.map {
        case errors: CreateUser.Error.ValidateErrors =>
          createJsonResponse(BAD_REQUEST, errors)(createUserValidateErrorsEncoder)
        case CreateUser.Error.DuplicateEmail =>
          createJsonResponse(CONFLICT, CreateUser.Error.DuplicateEmail)(createUserDuplicateEmailEncoder)
        case CreateUser.Error.Unknown(e) => {
          println(e)
          cc.responseBuilder.build(INTERNAL_SERVER_ERROR)
        }
      }
    } yield cc.responseBuilder.build(OK)
    result.merge
  }

  private def createJsonResponse[T](status: HttpStatus, result: T)(implicit encoder: JsonEncoder[T]): HttpResponse = {
    cc.responseBuilder
      .withContentType("application/json")
      .build(
        status = status,
        body = JsonFormatter.format(encoder.encode(result))
      )
  }

  private def parseJsonRequest[T](request: HttpRequest, decoder: JsonDecoder[T]): Either[HttpResponse, T] = {
    val badRequest = cc.responseBuilder.build(BAD_REQUEST, "json parse error")
    JsonParser
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
        JsObject(Map("errors" -> JsonEncoder.encode(errorsMap)))
      }
    }
  }

  val createUserDuplicateEmailEncoder: JsonEncoder[CreateUser.Error.DuplicateEmail.type] = {
    new JsonEncoder[CreateUser.Error.DuplicateEmail.type] {
      override def encode(value: CreateUser.Error.DuplicateEmail.type): JsValue = {
        JsObject(Map("errors" -> JsonEncoder.encode(Map("email" -> "メールアドレスが既に登録されています"))))
      }
    }
  }

}
