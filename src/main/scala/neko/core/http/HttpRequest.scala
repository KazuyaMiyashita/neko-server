package neko.core.http

import java.io.InputStream
import scala.io.BufferedSource

case class HttpRequest(
    line: HttpRequestLine,
    header: HttpRequestHeader,
    body: String
) {

  def asString: String = {
    List(line.asString, header.asString, "", body).mkString("\n")
  }

}

object HttpRequest {

  def fromInputStream(in: InputStream): HttpRequest = {
    val source                  = new BufferedSource(in)
    val firstHalf: List[String] = source.getLines.takeWhile(_.nonEmpty).toList
    val line                    = HttpRequestLine.fromString(firstHalf.head)
    val header                  = HttpRequestHeader.fromString(firstHalf.tail)
    val body = header.contentLength match {
      case None         => ""
      case Some(length) => source.take(length).mkString
    }
    HttpRequest(line, header, body)
  }

}
