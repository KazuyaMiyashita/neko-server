package neko.core.http

case class HttpRequestLine(
    method: HttpMethod,
    uri: String,
    httpVersion: String
) {

  def getPath: String = {
    uri.split('?')(0)
  }

  def getQueries: Map[String, String] = {
    def splitAmpersand(query: String): Map[String, String] = {
      query
        .split('&')
        .map { key_value =>
          val Array(key, value) = key_value.split('=')
          key -> value
        }
        .toMap
    }

    uri.split('?') match {
      case Array(_, query_flagment) => {
        query_flagment.split('#') match {
          case Array(query)    => splitAmpersand(query)
          case Array(query, _) => splitAmpersand(query)
          case _               => Map.empty
        }
      }
      case _ => Map.empty
    }
  }

  def getFlagment: Option[String] = {
    uri.split('#') match {
      case Array(_, flagment) => Some(flagment)
      case _                  => None
    }
  }

  def asString: String = s"${method.asString} ${uri} ${httpVersion}"

}

object HttpRequestLine {

  def fromString(line: String): HttpRequestLine = {
    val Array(method, uri, httpVersion) = line.split(" ")
    HttpRequestLine(HttpMethod.fromString(method), uri, httpVersion)
  }

}
