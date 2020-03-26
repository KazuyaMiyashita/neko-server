import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, BufferedWriter, OutputStreamWriter}
import neko.core.server.RequestHandler

class TelnetRequestHandler extends RequestHandler {

  override def handle(socket: Socket): Unit = {
    val in  = new BufferedReader(new InputStreamReader(socket.getInputStream()))
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))

    println("new socket")

    def proc(): Unit = {
      val line = in.readLine()
      println(line)
      if (line != "") {
        println(line)
        out.write(line + "\n")
        out.flush()
        proc()
      } else ()
      // Option(line) match {
      //   case Some(line) if line != "" => {
      //     println(line)
      //     out.write(line + "\n")
      //     out.flush()
      //     proc()
      //   }
      //   case _ => ()
      // }
    }
    proc()

    socket.close()
  }

}
