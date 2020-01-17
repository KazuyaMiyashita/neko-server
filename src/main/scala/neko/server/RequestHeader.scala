package neko.server

case class RequestHeader(
  method: Method,
  url: String,
  contentLength: Option[Int],
  contentType: Option[String]
)
