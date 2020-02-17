import neko.core.server._
import neko.core.http._

object Main extends App {

  val routes = Routes(
    GET  -> "/"     -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/echo" -> (req => Response(OK, req.body))
    // GET     -> "/rooms"      -> neko.controller.ChatRooms.roomList,
    // POST    -> "/rooms"      -> neko.controller.ChatRooms.create,
    // GET     -> "/rooms/.*".r -> neko.controller.ChatRooms.messageList,
    // POST    -> "/rooms/.*".r -> neko.controller.ChatRooms.messageSend,
    // OPTIONS -> ".*".r        -> neko.controller.HandlePreflight.apply
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
