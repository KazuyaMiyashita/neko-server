package neko.core.http

case class Response private (
    status: Status,
    headers: Map[String, String],
    body: Option[String]
) {

  def withContentType(contentType: String) = Response(
    status,
    headers + (("Content-Type") -> contentType),
    body
  )

  def withHeader(key: String, value: String) = Response(
    status,
    headers + (key -> value),
    body
  )

  def view: String = {
    import scala.collection.mutable.StringBuilder
    val str = new StringBuilder
    str append s"HTTP/1.1 ${status.view}\n"
    str append headers.map({ case (key, value) => key + ": " + value }).mkString("\n") + "\n\n"
    body.foreach { msg =>
      str append msg
    }
    str.mkString
  }

}

object Response {

  def apply(status: Status) = {
    val headers = Map(
      ("Content-Length"              -> "0"),
      ("Access-Control-Allow-Origin" -> "*"),
      ("Connection"                  -> "close")
    )
    new Response(status, headers, None)
  }
  def apply(status: Status, body: String) = {
    val headers = Map(
      ("Content-Length"              -> body.getBytes.length.toString),
      ("Access-Control-Allow-Origin" -> "*"),
      ("Connection"                  -> "close")
    )
    new Response(status, headers, Some(body))
  }

}

trait Status {
  def view: String
}
object OK extends Status {
  override def view = "200 OK"
}
object BAD_REQUEST extends Status {
  override def view = "400 Bad Request"
}
object NOT_FOUND extends Status {
  override def view = "404 Not Found"
}
object CONFLICT extends Status {
  override def view = "409 Conflict"
}
object INTERNAL_SERVER_ERROR extends Status {
  override def view = "500 Internal Server Error"
}
