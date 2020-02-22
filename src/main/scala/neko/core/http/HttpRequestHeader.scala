package neko.core.http

case class HttpRequestHeader(
    lines: Seq[String]
) {

  val fields: Map[String, Seq[String]] =
    lines
      .map { line =>
        val Array(key, value) = line.split(": ", 2)
        key -> value
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap

  def contentLength: Option[Int]  = fields.get("Content-Length").flatMap(_.headOption).map(_.toInt)
  def contentType: Option[String] = fields.get("Content-Type").flatMap(_.headOption)
  def cookies: Map[String, String] = {
    val list: Seq[String] = fields.get("Cookie").getOrElse(Seq.empty)
    list.map { line =>
      val Array(key, value) = line.split("=", 2)
      key -> value
    }.toMap
  }

  def asString = lines.mkString("\n")

}

object HttpRequestHeader {

  def fromString(lines: Seq[String]): HttpRequestHeader = HttpRequestHeader(lines)

}
