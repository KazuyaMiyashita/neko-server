package neko.server

import scala.util.matching.Regex

trait Method {

  def ->(url: String) = RouteBuilder(this, url.r)
  def ->(re: Regex)   = RouteBuilder(this, re)

}
object Method {
  def fromString(m: String): Method = m match {
    case "GET"     => GET
    case "POST"    => POST
    case "OPTIONS" => OPTIONS
    case _         => UNKNOWN
  }
}
object GET     extends Method
object POST    extends Method
object OPTIONS extends Method
object UNKNOWN extends Method
