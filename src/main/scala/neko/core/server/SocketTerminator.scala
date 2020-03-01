package neko.core.server

import scala.collection.mutable
import java.net.Socket

class SocketTerminator {

  val _sockets = mutable.Set.empty[Socket]
  def sockets = synchronized(_sockets)

  def register(s: Socket): Unit = sockets.add(s)
  def release(s: Socket): Unit  = sockets.remove(s)

  def terminateAll(): Unit = {
    sockets.foreach { s =>
      if (!s.isClosed()) s.close()
    }
    sockets.clear()
  }

}
