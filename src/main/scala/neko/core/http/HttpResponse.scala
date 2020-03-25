package neko.core.http

case class HttpResponse(
    status: HttpStatus,
    headers: Map[String, String],
    body: Option[String]
) {

  def withContentType(contentType: String) = HttpResponse(
    status,
    headers + (("Content-Type") -> contentType),
    body
  )

  def withHeader(key: String, value: String) = HttpResponse(
    status,
    headers + (key -> value),
    body
  )

  def asString: String = {
    import scala.collection.mutable.StringBuilder
    val str = new StringBuilder
    str append s"HTTP/1.1 ${status.asString}\n"
    str append headers.map({ case (key, value) => key + ": " + value }).mkString("\n") + "\n\n"
    body.foreach { msg =>
      str append msg
    }
    str.mkString
  }

}

object HttpResponse {

  def apply(status: HttpStatus) = {
    val headers = Map(
      ("Content-Length"                   -> "0"),
      ("Access-Control-Allow-Origin"      -> "http://localhost:8000"), // これアプリケーションじゃなくてcoreの方にあるの何で？
      ("Access-Control-Allow-Credentials" -> "true"),
      ("Connection"                       -> "close")
    )
    new HttpResponse(status, headers, None)
  }
  def apply(status: HttpStatus, body: String) = {
    val headers = Map(
      ("Content-Length"                   -> body.getBytes.length.toString),
      ("Access-Control-Allow-Origin"      -> "http://localhost:8000"),
      ("Access-Control-Allow-Credentials" -> "true"),
      ("Connection"                       -> "close")
    )
    new HttpResponse(status, headers, Some(body))
  }

}

trait HttpStatus {
  def asString: String
}
object OK extends HttpStatus {
  override def asString = "200 OK"
}
object BAD_REQUEST extends HttpStatus {
  override def asString = "400 Bad Request"
}
object UNAUTHORIZED extends HttpStatus {
  override def asString = "401 Unauthorized"
}
object NOT_FOUND extends HttpStatus {
  override def asString = "404 Not Found"
}
object CONFLICT extends HttpStatus {
  override def asString = "409 Conflict"
}
object INTERNAL_SERVER_ERROR extends HttpStatus {
  override def asString = "500 Internal Server Error"
}
