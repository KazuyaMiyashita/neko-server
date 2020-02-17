package neko.http

case class Request(
    header: RequestHeader,
    body: String
)
