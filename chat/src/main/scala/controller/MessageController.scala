package neko.chat.controller

import java.util.UUID
import java.time.Clock

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR}
import neko.core.json.Json
import neko.core.jdbc.DBPool

import neko.chat.repository.MessageRepository
import neko.chat.entity.User
import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue

class MessageController(
    messageRepository: MessageRepository,
    dbPool: DBPool,
    clock: Clock
) {

  import UserController._

  def get(request: Request): Response = {
    println(request.header.getQueries)
    val result: Either[Response, Response] = for {
      id <- request.header.getQueries
        .get("id")
        .map(UUID.fromString)
        .toRight(Response(BAD_REQUEST))
      user <- userRepository
        .fetchBy(id)
        .runReadOnly(dbPool.getConnection())
        .left
        .map { _ =>
          Response(INTERNAL_SERVER_ERROR)
        }
        .flatMap(_.toRight(Response(NOT_FOUND)))
    } yield {
      val jsonString = Json.format(userEncoder.encode(user))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

  def create(request: Request): Response = {
    val result: Either[Response, Response] = for {
       <- Json
        .parse(request.body)
        .flatMap(nameDecoder.decode)
        .toRight(Response(BAD_REQUEST))
      user     = User(UUID.randomUUID(), name, clock.instant())
      dbResult = userRepository.create(user).runTx(dbPool.getConnection())
      _ <- dbResult.left.map { e =>
        Response(INTERNAL_SERVER_ERROR)
      }
    } yield {
      val jsonString = Json.format(userEncoder.encode(user))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }

}

object MessageRepository {

  case class CreateMessageRequest(body: String)

  val userEncoder: JsonEncoder[User] = new JsonEncoder[User] {
    override def encode(user: User): JsValue = Json.obj(
      "id"   -> Json.str(user.id.toString),
      "screen_name" -> Json.str(user.screenName)
    )
  }

}
