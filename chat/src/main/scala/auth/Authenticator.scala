package neko.chat.auth

import neko.chat.entity.User
import neko.core.http.{HttpRequest, HttpResponse}

trait Authenticator {

  def auth(request: HttpRequest): Either[HttpResponse, User]

}
