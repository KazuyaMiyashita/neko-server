package neko.core.server

import java.net.Socket
import java.io.InputStream
import java.io.OutputStream
import java.net.SocketException

class SocketHandler(
    socket: Socket,
    requestHandler: IRequestHandler,
    socketManager: SocketTerminator
) extends Thread {

  override def run(): Unit = {
    val request = new IRequest {
      override def in: InputStream   = socket.getInputStream()
      override def out: OutputStream = socket.getOutputStream()
      override def close(): Unit     = terminate()
    }
    try {
      requestHandler.handle(request)
    } catch {
      case _: SocketException => println("socket closed")
    } finally {
      terminate()
    }
  }

  def terminate(): Unit = {
    socket.close()
    socketManager.release(socket)
  }

}
