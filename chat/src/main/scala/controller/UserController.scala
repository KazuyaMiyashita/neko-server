package neko.chat.controller

import neko.core.http.{Request, Response}
import neko.core.http.{OK, BAD_REQUEST, INTERNAL_SERVER_ERROR}
import neko.core.json.Json

import neko.chat.repository.UserRepository
import neko.chat.entity.User

class UserController(
    userRepository: UserRepository
) {

  import UserController._

  def create(request: Request): Response = {

    val result = for {
      form <- UserCreateRequest.parse(request.body).toRight(Response(BAD_REQUEST))
      user <- userRepository.create(form.name).left.map { _ =>
        Response(INTERNAL_SERVER_ERROR)
      }
    } yield {
      val res = UserCreateResponse.fromUser(user)
      Response(OK, res.toJsonString).withContentType("application/json")
    }

    result.merge
  }

}

object UserController {

  case class UserCreateRequest(name: String)
  object UserCreateRequest {
    def parse(body: String): Option[UserCreateRequest] = {
      Json
        .parse(body)
        .flatMap(json => (json \ "name").as[String])
        .map(name => UserCreateRequest(name))
    }
  }

  case class UserCreateResponse(
      id: String,
      name: String
  ) {
    def toJsonString: String = {
      val json = Json.obj(
        "id"   -> Json.str(id),
        "name" -> Json.str(name)
      )
      Json.format(json)
    }
  }
  object UserCreateResponse {
    def fromUser(user: User): UserCreateResponse =
      UserCreateResponse(user.id, user.name)
  }

}
