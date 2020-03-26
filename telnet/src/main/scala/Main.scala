import neko.core.server.ServerSocketHandler

object Main extends App {

  val requestHandler      = new TelnetRequestHandler
  val serverSocketHandler = new ServerSocketHandler(requestHandler, 2222)
  serverSocketHandler.start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
