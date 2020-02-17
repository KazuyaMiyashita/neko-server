package neko.server

import java.net.Socket

class SocketTerminator {

  import scala.collection.mutable
  val sockets = mutable.Set.empty[Socket]

  def register(s: Socket): Unit = sockets.add(s)
  def release(s: Socket): Unit  = sockets.remove(s)

  def terminateAll(): Unit = {
    sockets.foreach { s =>
      if (!s.isClosed()) s.close()
    }
  }

}
