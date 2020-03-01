package neko.core.server

import scala.collection.mutable
import java.net.Socket

class SocketTerminator {

  private val _sockets = mutable.Set.empty[Socket]
  private def sockets  = synchronized(_sockets)

  def register(s: Socket): Unit = sockets.add(s)
  def release(s: Socket): Unit  = sockets.remove(s)

  def terminateAll(): Unit = {
    sockets.foreach(_.close())
    sockets.clear()
  }

}
