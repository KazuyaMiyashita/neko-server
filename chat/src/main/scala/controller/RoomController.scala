package neko.chat.controller

import java.time.Clock
import java.util.UUID

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR}
import neko.core.jdbc.DBPool
import neko.core.json.{Json, JsValue, JsonDecoder, JsonEncoder}
import neko.chat.entity.Room
import neko.chat.auth.Authenticator
import neko.chat.repository.RoomRepository
import neko.chat.repository.RoomRepository.DuplicateRoomNameException

class RoomController(
    roomRepository: RoomRepository,
    authenticator: Authenticator,
    dbPool: DBPool,
    clock: Clock
) {

  import RoomController._

  def createRoom(request: Request): Response = {
    val result = for {
      user <- authenticator.auth(request)
      roomName <- Json
        .parse(request.body)
        .flatMap(nameDecoder.decode)
        .toRight(Response(BAD_REQUEST))
      room = Room(UUID.randomUUID(), roomName, clock.instant())
      _ <- roomRepository
        .create(room)
        .runTx(dbPool.getConnection())
        .left
        .map {
          case e: DuplicateRoomNameException => Response(CONFLICT, "ルームネームが既に使用されています")
          case _                             => Response(INTERNAL_SERVER_ERROR)
        }
    } yield {
      val jsonString = Json.format(roomEncoder.encode(room))
      Response(OK, jsonString).withContentType("application/json")
    }
    result.merge
  }
}

object RoomController {

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

  val roomEncoder: JsonEncoder[Room] = new JsonEncoder[Room] {
    override def encode(value: Room): JsValue = Json.obj(
      "id"   -> Json.str(value.id.toString),
      "name" -> Json.str(value.name)
    )
  }

}
