package neko.http

import neko.server.{IRequest, IRequestHandler}
import scala.io.BufferedSource
import java.io.{BufferedWriter, OutputStreamWriter}

class HttpRequestHandler(routes: Routes) extends IRequestHandler {

  override def handle(req: IRequest): Unit = {
    val in    = new BufferedSource(req.in)
    val lines = in.getLines.takeWhile(_.nonEmpty).toList
    println("**request**")
    lines.foreach(println)
    println()
    val header = RequestHeaderParser.parse(lines)
    val body = header.contentLength match {
      case None         => ""
      case Some(length) => in.take(length).mkString
    }
    println(body)

    val request  = Request(header, body)
    val response = routes(request)

    val out = new BufferedWriter(new OutputStreamWriter(req.out))
    out.write(response.view)
    out.flush()

    println("**response**")
    println(response.view)

    req.close()

  }

}
