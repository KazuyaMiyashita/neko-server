package neko.http

case class RequestHeader(
    method: Method,
    url: String,
    contentLength: Option[Int],
    contentType: Option[String]
) {

  def getPath: String = {
    url.split('?')(0)
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

    url.split('?') match {
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
    url.split('#') match {
      case Array(_, flagment) => Some(flagment)
      case _                  => None
    }
  }

}
