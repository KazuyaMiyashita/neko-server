package neko.core.server

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.net.SocketException

class ServerSocketHandler(
    requestHandler: IRequestHandler
) extends Thread {

  val serverSocket: ServerSocket             = new ServerSocket(2200)
  val socketExecutorService: ExecutorService = Executors.newFixedThreadPool(32)

  var isActive                = true
  var acceptionSocket: Socket = null
  val socketTerminator        = new SocketTerminator

  override def run(): Unit = {
    try {
      while (isActive) {
        acceptionSocket = serverSocket.accept()
        if (isActive) {
          socketTerminator.register(acceptionSocket)
          socketExecutorService.execute(new SocketHandler(acceptionSocket, requestHandler, socketTerminator))
        }
      }
    } catch {
      case _: SocketException => println("server socket closed")
    } finally {
      terminate()
    }
  }

  def terminate(): Unit = {
    isActive = false
    serverSocket.close()
    socketExecutorService.shutdown()
    if ((acceptionSocket ne null) && !acceptionSocket.isClosed()) {
      acceptionSocket.close()
    }
    socketTerminator.terminateAll()
  }

}
