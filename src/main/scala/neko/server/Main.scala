package neko.server

object Main extends App {

  val routes = Routes(
    GET -> "/" -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/echo" -> (req => Response(OK, req.body))
  )
  new Server(routes)

}
