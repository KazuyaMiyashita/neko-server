package neko.chat.auth

import neko.chat.entity.User
import neko.core.http.{Request, Response}

trait Authenticator {

  def auth(request: Request): Either[Response, User]

}
