package neko.server

case class Response(
    status: Status,
    message: Option[String],
    contentType: Option[String]
) {

  def writeString = {

    val msg = message.getOrElse("")

    s"""HTTP/1.1 ${status.writeString}
         |Content-Length: ${msg.length()}
         |Content-Type: ${contentType.getOrElse("text/plain")}
         |
         |${msg}""".stripMargin
  }

}

object Response {
  def apply(status: Status)                  = new Response(status, None, None)
  def apply(status: Status, message: String) = new Response(status, Some(message), None)
  def apply(status: Status, message: String, contentType: String) =
    new Response(status, Some(message), Some(contentType))
}

trait Status {
  def writeString: String
}
object OK extends Status {
  override def writeString = "200 OK"
}
object BAD_REQUEST extends Status {
  override def writeString = "400 Bad Request"
}
object NOT_FOUND extends Status {
  override def writeString = "404 Not Found"
}
