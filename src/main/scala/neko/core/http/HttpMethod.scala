package neko.core.http

import scala.util.matching.Regex

sealed trait HttpMethod {

  def ->(url: String) = RouteBuilder(this, url.r)
  def ->(re: Regex)   = RouteBuilder(this, re)

  def asString: String

}
object HttpMethod {
  def fromString(m: String): HttpMethod = m match {
    case "GET"     => GET
    case "POST"    => POST
    case "OPTIONS" => OPTIONS
    case _         => _UNKNOWN
  }
}
object GET extends HttpMethod {
  override def asString = "GET"
}
object POST extends HttpMethod {
  override def asString = "POST"
}
object OPTIONS extends HttpMethod {
  override def asString = "OPTIONS"
}
object _UNKNOWN extends HttpMethod {
  override def asString = "UNKNOWN"
}
