package neko.controller

import neko.server.{Request, Response}
import neko.json.Json
import neko.server.OK
import neko.server.BAD_REQUEST

object ChatRooms {

  def roomList(request: Request): Response = {
    val rooms: List[String] = ChatMaster.table.map(_.room).distinct.toList
    val json                = Json.arr(rooms.map(r => Json.str(r)): _*)
    Response(OK, Json.format(json))
      .withContentType("application/json")
  }

  def create(request: Request): Response = {
    val roomName = Json.parse(request.body).flatMap(_.as[String].toRight("bad format"))
    roomName match {
      case Left(_) => Response(BAD_REQUEST)
      case Right(rn) => {
        ChatMaster.table += ChatMaster(rn, "チャットマスター", "000000", "ルームが作成されました", System.currentTimeMillis)
        Response(OK)
      }
    }
  }

  def messageList(request: Request): Response = {
    val room                      = request.header.getPath.substring("/rooms/".length)
    val maybeOffset: Option[Long] = request.header.getQueries.get("offset").map(_.toLong)

    val messages = maybeOffset match {
      case None         => ChatMaster.table.toList.filter(_.room == room)
      case Some(offset) => ChatMaster.table.toList.filter(_.room == room).filter(_.timestamp > offset)
    }
    val messagesJson = Json.arr(messages.toList.map { mes =>
      Json.obj(
        "name"      -> Json.str(mes.name),
        "hash"      -> Json.str(mes.hash),
        "message"   -> Json.str(mes.message),
        "timestamp" -> Json.num(mes.timestamp)
      )
    }: _*)
    Response(OK, Json.format(messagesJson)).withContentType("application/json")
  }

  def messageSend(request: Request): Response = {
    val room = request.header.url.substring("/rooms/".length)
    import neko.json.{Json, JsonDecoder, JsValue, JsObject}
    val messageDecoder: JsonDecoder[ChatMaster] = new JsonDecoder[ChatMaster] {
      override def decode(js: JsValue): Option[ChatMaster] = js match {
        case obj: JsObject =>
          for {
            name    <- (obj \ "name").as[String]
            message <- (obj \ "message").as[String]
          } yield ChatMaster(room, name, "cccccc", message, System.currentTimeMillis)
        case _ => None
      }
    }
    val messageOpt: Option[ChatMaster] = Json.parse(request.body).toOption.flatMap(Json.decode(_)(messageDecoder))
    messageOpt match {
      case None => Response(BAD_REQUEST)
      case Some(message) => {
        ChatMaster.table += message
        Response(OK)
      }
    }
  }

}
