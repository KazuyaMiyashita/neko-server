package neko.chat.controller

import neko.core.http._

class Routing(
    userController: UserController,
    authController: AuthController,
    messageController: MessageController,
    cc: ControllerComponent
) extends HttpApplication {

  override def handle(request: HttpRequest): HttpResponse = {

    val router: HttpRouter = {
      import RoutingDSL._
      HttpRouter(
        GET  -> "/"             -> (_ => cc.responseBuilder.build(OK, "Hello My Server!")),
        POST -> "/users"        -> userController.create,
        POST -> "/auth/login"   -> authController.login,
        POST -> "/auth/logout"  -> authController.logout,
        GET  -> "/auth/session" -> authController.session,
        GET  -> "/messages"     -> messageController.get,
        POST -> "/messages"     -> messageController.post
      )
    }

    router.handle(request)(onNotFound = cc.responseBuilder.build(NOT_FOUND), onError = { e: Throwable =>
      e.printStackTrace()
      cc.responseBuilder.build(INTERNAL_SERVER_ERROR)
    })

  }

}
