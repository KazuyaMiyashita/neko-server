package neko.chat.controller

import java.util.UUID
import java.time.Clock

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, INTERNAL_SERVER_ERROR}
import neko.core.json.Json
import neko.core.jdbc.DBPool

import neko.chat.repository.UserRepository
import neko.chat.entity.User
import neko.core.json.{JsonDecoder, JsonEncoder}
import neko.core.json.JsValue

class UserController(
    userRepository: UserRepository,
    dbPool: DBPool,
    clock: Clock
) {

  import UserController._

  def create(request: Request): Response = {
    val result: Either[Response, Response] = for {
      name <- Json
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

object UserController {

  val nameDecoder: JsonDecoder[String] = new JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = {
      (js \ "name").as[String]
    }
  }

  val userEncoder: JsonEncoder[User] = new JsonEncoder[User] {
    override def encode(user: User): JsValue = Json.obj(
      "id"          -> Json.str(user.id.toString),
      "screen_name" -> Json.str(user.screenName)
    )
  }

}
