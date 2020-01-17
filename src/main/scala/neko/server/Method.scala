package neko.server

trait Method {

  def ->(url: String) = RouteBuilder(this, url)

}
object Method {
  def fromString(m: String): Method = m match {
    case "GET" => GET
    case "POST" => POST
    case _ => throw new Exception("Method Not Allowed")
  }
}
object GET extends Method
object POST extends Method
