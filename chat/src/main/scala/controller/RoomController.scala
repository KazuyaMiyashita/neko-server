package neko.chat.controller

import java.time.Clock

import neko.core.http.{Request, Response}
import neko.core.http.OK
import neko.core.jdbc.DBPool

import neko.chat.repository.UserRepository

import neko.chat.auth.Authenticator

class RoomController(
    userRepository: UserRepository,
    authenticator: Authenticator,
    dbPool: DBPool,
    clock: Clock
) {

  def createRoom(request: Request): Response = {
    val result = for {
      user <- authenticator.auth(request)
    } yield {
      Response(OK)
    }
    result.merge
  }
}
