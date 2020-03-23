package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR}
import neko.core.json.Json

import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue
import neko.core.http.HttpStatus

import neko.chat.application.entity.Token
import neko.chat.application.usecase.{FetchUserIdByToken, GetMessages, PostMessage}

class MessageController(
    fetchUserIdByToken: FetchUserIdByToken,
    getMessages: GetMessages,
    postMessage: PostMessage
) {

  import MessageController._

  def get(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = getMessages
      .latest50messages()
      .map(list => createJsonResponse(OK, list))
      .left
      .map {
        case GetMessages.Error.Unknown(e) => println(e); HttpResponse(INTERNAL_SERVER_ERROR)
      }
    result.merge
  }

  def post(request: HttpRequest): HttpResponse = {
    val result: Either[HttpResponse, HttpResponse] = for {
      postRequest <- parseJsonRequest(request, postRequestDecoder)
      token <- request.header.cookies
        .get("token")
        .map(Token.apply)
        .toRight(HttpResponse(UNAUTHORIZED))
      userId <- fetchUserIdByToken.execute(token).toRight(HttpResponse(UNAUTHORIZED))
      messages = postMessage.execute(PostMessage.Request(userId, postRequest.body))
    } yield {
      HttpResponse(OK).withContentType("application/json")
    }
    result.merge
  }

}

object MessageController {

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

  implicit val messageResponseEncoder: JsonEncoder[GetMessages.MessageResponse] =
    new JsonEncoder[GetMessages.MessageResponse] {
      override def encode(message: GetMessages.MessageResponse): JsValue = Json.obj(
        "id"        -> Json.str(message.id.asString),
        "body"      -> Json.str(message.body.value),
        "userName"  -> Json.str(message.userName.value),
        "createdAt" -> Json.num(message.createdAt.toEpochMilli)
      )
    }

  case class PostRequest(body: String)
  val postRequestDecoder: JsonDecoder[PostRequest] = new JsonDecoder[PostRequest] {
    override def decode(js: JsValue): Option[PostRequest] = {
      for {
        body <- (js \ "body").as[String]
      } yield PostRequest(body)
    }
  }

}
