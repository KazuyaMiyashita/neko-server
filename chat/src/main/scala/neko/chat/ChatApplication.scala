package neko.chat

import neko.chat.controller.{UserController, AuthController, MessageController}
import neko.core.http._

class ChatApplication(
    userController: UserController,
    authController: AuthController,
    messageController: MessageController
) extends HttpApplication {

  override def handle(request: HttpRequest): HttpResponse = {

    val router: Router = {
      import RoutingDSL._
      Router(
        GET  -> "/"             -> (_ => HttpResponse(OK, "Hello My Server!")),
        POST -> "/users"        -> userController.create,
        POST -> "/auth/login"   -> authController.login,
        POST -> "/auth/logout"  -> authController.logout,
        GET  -> "/auth/session" -> authController.session,
        GET  -> "/messages"     -> messageController.get,
        POST -> "/messages"     -> messageController.post
      )
    }

    try {
      router.handle(request).getOrElse(HttpResponse(NOT_FOUND))
    } catch {
      case e: Throwable => {
        e.printStackTrace()
        HttpResponse(INTERNAL_SERVER_ERROR)
      }
    }

  }

}
