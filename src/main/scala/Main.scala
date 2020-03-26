import java.net.{Socket, ServerSocket}
import java.io.{BufferedReader, InputStreamReader, BufferedWriter, OutputStreamWriter}

object ServerSample extends App {
  val server         = new ServerSocket(80)
  val socket: Socket = server.accept()
  val in             = new BufferedReader(new InputStreamReader(socket.getInputStream()))
  def proc(): Unit = {
    Option(in.readLine()) match {
      case Some(line) if line != "" => println(line); proc()
      case _                        => ()
    }
  }
  proc()

  val result =
    """HTTP/1.1 200 OK
      |Content-Type: text/html
      |
      |<h1>It works!</h1>
      |""".stripMargin
  val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
  out.write(result)
  out.flush()
  socket.close()
  server.close()
}
