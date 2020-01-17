package neko.server

case class Request(
  header: RequestHeader,
  body: String
)
