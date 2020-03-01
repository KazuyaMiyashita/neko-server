package neko.core.server

import java.net.Socket
import java.net.SocketException

class SocketHandler(
    socket: Socket,
    requestHandler: RequestHandler,
    socketTerminator: SocketTerminator
) extends Thread {

  socketTerminator.register(socket)

  override def run(): Unit = {
    try {
      requestHandler.handle(socket)
    } catch {
      case _: SocketException => println("socket closed")
    } finally {
      terminate()
    }
  }

  def terminate(): Unit = {
    if (!socket.isClosed()) {
      socket.close()
      socketTerminator.release(socket)
    }
  }

}
