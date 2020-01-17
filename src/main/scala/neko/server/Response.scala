package neko.server

case class Response(
  status: Status,
  message: Option[String]
) {

  def writeString = {

      val msg = message.getOrElse("")

      s"""HTTP/1.1 ${status.writeString}
         |Content-Length: ${msg.length()}
         |Content-Type: text/plain"
         |
         |${msg}
         |""".stripMargin
  }

}

object Response {
  def apply(status: Status) = new Response(status, None)
  def apply(status: Status, message: String) = new Response(status, Some(message))
}

trait Status {
  def writeString: String
}
object OK extends Status {
  override def writeString = "200 OK"
}
object NOT_FOUND extends Status {
  override def writeString = "404 Not Found"
}
