package neko.server

import java.net.ServerSocket
import scala.io.BufferedSource
import java.io.{BufferedWriter, OutputStreamWriter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Server(routes: Routes) {

  println("start >>>")

  val server = new ServerSocket(2200)
  Future {
    while (true) {
      val socket = server.accept()
      val in     = new BufferedSource(socket.getInputStream())
      val lines  = in.getLines.takeWhile(_.nonEmpty).toList
      println("**request**")
      lines.foreach(println)
      val header = RequestHeaderParser.parse(lines)
      val body = header.contentLength match {
        case None         => ""
        case Some(length) => in.take(length).mkString
      }
      println(body)

      val request  = Request(header, body)
      val response = routes(request)

      val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
      out.write(response.writeString)
      out.flush()

      println("**response**")
      println(response.writeString)
    }
  }

  io.StdIn.readLine()
  server.close()
  println("<<< end")

}
