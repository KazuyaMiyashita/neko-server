import java.net.Socket
import java.io.{BufferedReader, InputStreamReader, BufferedWriter, OutputStreamWriter}
import neko.core.server.RequestHandler

class TelnetRequestHandler extends RequestHandler {

  override def handle(socket: Socket): Unit = {
    val in  = new BufferedReader(new InputStreamReader(socket.getInputStream()))
    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))

    def proc(): Unit = {
      Option(in.readLine()) match {
        case Some(line) if line != "" => {
          Thread.sleep(5000)
          out.write(line + "\n")
          out.flush()
          proc()
        }
        case _ => ()
      }
    }
    proc()

    socket.close()
  }

}
