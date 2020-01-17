package neko.server

import java.net.{Socket, ServerSocket}
import scala.io.BufferedSource
import java.io.{BufferedWriter, OutputStreamWriter}

class Server(routes: Routes) {

  println("start >>>")

  val server = new ServerSocket(2200)
  try {
    val socket = server.accept()
    val in = new BufferedSource(socket.getInputStream())
    val header = RequestHeaderParser.parse(in.getLines.takeWhile(_.nonEmpty).toList)
    val body = header.contentLength match {
      case None => ""
      case Some(length) => in.take(length).mkString
    }
    val request = Request(header, body)
    val response = routes(request)

    val out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
    out.write(response.writeString)

    println("header:")
    println(header)
    println("body: [%s]".format(body))

    println("response:")
    println(response.writeString)
    out.flush()

  } finally {
    server.close()
  }

  println("<<< end")

}
