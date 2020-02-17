package neko.server

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.net.SocketException

class ServerSocketHandler(
    requestHandler: ISocketRequestHandler
) extends Thread {

  val serverSocket: ServerSocket             = new ServerSocket(2200)
  val socketExecutorService: ExecutorService = Executors.newFixedThreadPool(32)

  var isActive                = true
  var acceptionSocket: Socket = null
  val socketManagaer          = new SocketManager

  override def run(): Unit = {
    try {
      while (isActive) {
        acceptionSocket = serverSocket.accept()
        if (isActive) {
          socketManagaer.register(acceptionSocket)
          socketExecutorService.execute(new SocketHandler(acceptionSocket, requestHandler, socketManagaer))
        }
      }
    } catch {
      case _: SocketException => println("server socket closed")
    } finally {
      close()
    }
  }

  def close(): Unit = {
    isActive = false
    serverSocket.close()
    socketExecutorService.shutdown()
    if ((acceptionSocket ne null) && !acceptionSocket.isClosed()) {
      acceptionSocket.close()
    }
    socketManagaer.closeAll()
  }

}

class SocketHandler(
    socket: Socket,
    requestHandler: ISocketRequestHandler,
    socketManager: SocketManager
) extends Thread {

  override def run(): Unit = {
    val request = new ISocketRequest {
      override def in: InputStream   = socket.getInputStream()
      override def out: OutputStream = socket.getOutputStream()
      override def close(): Unit     = onClose()
    }
    try {
      requestHandler.handle(request)
    } catch {
      case _: SocketException => println("socket closed")
    } finally {
      onClose()
    }
  }

  def onClose(): Unit = {
    socket.close()
    socketManager.release(socket)
  }

}

class SocketManager {

  import scala.collection.mutable
  val sockets = mutable.Set.empty[Socket]

  def register(s: Socket): Unit = sockets.add(s)
  def release(s: Socket): Unit  = sockets.remove(s)

  def closeAll(): Unit = {
    sockets.foreach { s =>
      if (!s.isClosed()) s.close()
    }
  }

}

trait ISocketRequest {
  def in: InputStream
  def out: OutputStream
  def close(): Unit
}

trait ISocketRequestHandler {
  def handle(request: ISocketRequest): Unit
}
