package neko.chat

import neko.core.server._
import neko.core.http._

object Main extends App {

  val routes = Routes(
    GET  -> "/"     -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/echo" -> (req => Response(OK, req.body))
  )

  val requestHandler: IRequestHandler = new HttpRequestHandler(routes)

  val serverSocketHandler = new ServerSocketHandler(
    requestHandler
  )

  new Thread(serverSocketHandler).start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
