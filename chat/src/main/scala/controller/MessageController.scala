package neko.chat.controller

import java.util.UUID
import java.time.Clock

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, INTERNAL_SERVER_ERROR}
import neko.core.json.Json
import neko.core.jdbc.DBPool

import neko.chat.auth.Authenticator
import neko.chat.repository.MessageRepository
import neko.chat.repository.MessageRepository.MessageResponse
import neko.chat.entity.Message
import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue

class MessageController(
    messageRepository: MessageRepository,
    authenticator: Authenticator,
    dbPool: DBPool,
    clock: Clock
) {

  import MessageController._

  def get(request: Request): Response = {
    val result: Either[Response, Response] = for {
      user <- authenticator.auth(request)
      messages <- messageRepository.get().runReadOnly(dbPool.getConnection()).left.map { e =>
        Response(INTERNAL_SERVER_ERROR)
      }
    } yield {
      val jsonString = Json.format(postResponseEncoder.encode(messages))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

  def post(request: Request): Response = {
    val result: Either[Response, Response] = for {
      user <- authenticator.auth(request)
      postRequest <- Json
        .parse(request.body)
        .flatMap(postRequestDecoder.decode)
        .toRight(Response(BAD_REQUEST))
      newMessage = Message(UUID.randomUUID(), user.id, postRequest.body, clock.instant())
      _ <- messageRepository.post(newMessage).runTx(dbPool.getConnection()).left.map { e =>
        Response(INTERNAL_SERVER_ERROR)
      }
    } yield {
      Response(OK).withContentType("application/json")
    }
    result.merge
  }

}

object MessageController {

  val postResponseEncoder: JsonEncoder[List[MessageResponse]] = {
    val messageResponseEncoder: JsonEncoder[MessageResponse] = new JsonEncoder[MessageResponse] {
      override def encode(value: MessageResponse): JsValue = Json.obj(
        "id"                  -> Json.str(value.message.id.toString),
        "body"                -> Json.str(value.message.body),
        "userScreenName"      -> Json.str(value.user.screenName),
        "createdAtEpochMilli" -> Json.num(value.message.createdAt.toEpochMilli)
      )
    }

    new JsonEncoder[List[MessageResponse]] {
      override def encode(values: List[MessageResponse]): JsValue =
        Json.arr(values.map(messageResponseEncoder.encode(_)): _*)
    }
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
