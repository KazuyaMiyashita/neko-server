package neko.core.http

case class Request(
    header: RequestHeader,
    body: String
)
