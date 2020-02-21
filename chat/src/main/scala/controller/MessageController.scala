package neko.chat.controller

import java.util.UUID
import java.time.Clock

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR}
import neko.core.json.Json
import neko.core.jdbc.DBPool

import neko.chat.auth.Authenticator
import neko.chat.repository.MessageRepository
import neko.chat.entity.{User, Message}
import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue

class MessageController(
    messageRepository: MessageRepository,
    authenticator: Authenticator,
    dbPool: DBPool,
    clock: Clock
) {

  import MessageRepository._

  def get(request: Request): Response = {
    val result: Either[Response, Response] = for {
      user <- authenticator.auth(request)
      messages <- messageRepository.get().runReadOnly(dbPool.getConnection())
        .left.map { e => Response(INTERNAL_SERVER_ERROR) }
    } yield {
      val jsonString = Json.format(userEncoder.encode(user))
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
      _ <- messageRepository.post(newMessage).runTx(dbPool.getConnection())
        .left.map { e => Response(INTERNAL_SERVER_ERROR) }
    } yield {
      val jsonString = Json.format(userEncoder.encode(user))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

}

object MessageRepository {

  case class PostRequest(body: String)
  val postRequestDecoder: JsonDecoder[PostRequest] = new JsonDecoder[PostRequest] {
    override def decode(js: JsValue): Option[PostRequest] = {
      for {
        body <- (js \ "body").as[String]
      } yield PostRequest(body)
    }
  }

  val userEncoder: JsonEncoder[User] = new JsonEncoder[User] {
    override def encode(user: User): JsValue = Json.obj(
      "id"   -> Json.str(user.id.toString),
      "screen_name" -> Json.str(user.screenName)
    )
  }

}
