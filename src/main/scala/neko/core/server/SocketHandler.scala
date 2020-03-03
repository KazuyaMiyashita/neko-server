package neko.core.server

import java.net.Socket
import java.net.SocketException

class SocketHandler(
    socket: Socket,
    requestHandler: RequestHandler,
    socketTerminator: SocketTerminator
) extends Thread {

  override def run(): Unit = {
    try {
      socketTerminator.register(socket)
      requestHandler.handle(socket)
    } catch {
      case _: SocketException => println("socket closed")
    } finally {
      socket.close()
      socketTerminator.release(socket)
    }
  }

}
