package neko.chat.controller

import neko.core.http.{HttpRequest, HttpResponse}
import neko.core.http.{HttpStatus, OK, BAD_REQUEST, UNAUTHORIZED}
import neko.core.json.Json

import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue
import neko.core.http.HttpStatus

import neko.chat.application.entity.Token
import neko.chat.application.service.{FetchUserIdByToken, GetMessages, PostMessage}
import neko.chat.application.service.GetMessages.MessageResponse

class MessageController(
    fetchUserIdByToken: FetchUserIdByToken,
    getMessages: GetMessages,
    postMessage: PostMessage
) {

  import MessageController._

  def get(request: HttpRequest): HttpResponse = {
    val list: List[MessageResponse] = getMessages.latest50messages()
    createJsonResponse(OK, list)
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

  implicit val messageResponseEncoder: JsonEncoder[MessageResponse] = new JsonEncoder[MessageResponse] {
    override def encode(message: MessageResponse): JsValue = Json.obj(
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
