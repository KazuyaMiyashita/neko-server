package neko.server

object RequestHeaderParser {

  def parse(lines: Seq[String]): RequestHeader = {
    val Array(method, url, _) = lines.head.split(" ")
    val requestHeaderField: Map[String, String] = 
      lines.tail.map { line =>
        val Array(key, value) = line.split(": ", 2)
        key -> value
      }.toMap
    val contentLength = requestHeaderField.get("Content-Length").map(_.toInt)
    val contentType = requestHeaderField.get("Content-Type")
    RequestHeader(Method.fromString(method), url, contentLength, contentType)
  }

}
