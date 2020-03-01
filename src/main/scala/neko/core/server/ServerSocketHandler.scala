package neko.core.server

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.net.SocketException

class ServerSocketHandler(
    requestHandler: RequestHandler
) extends Thread {

  val server: ServerSocket                   = new ServerSocket(2200)
  val socketExecutorService: ExecutorService = Executors.newFixedThreadPool(32)

  var acceptionSocket: Socket = null
  val socketTerminator        = new SocketTerminator

  override def run(): Unit = {
    try {
      while (!server.isClosed()) {
        acceptionSocket = server.accept()
        socketTerminator.register(acceptionSocket)
        socketExecutorService.execute(new SocketHandler(acceptionSocket, requestHandler, socketTerminator))
      }
    } catch {
      case _: SocketException => println("server socket closed")
    } finally {
      terminate()
    }
  }

  def terminate(): Unit = {
    if (!server.isClosed()) {
      server.close()
      socketExecutorService.shutdown()
      socketTerminator.terminateAll()
    }
  }

}
