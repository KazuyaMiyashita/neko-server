package neko.core.http

case class HttpRequestHeader(
    lines: Seq[String]
) {

  val fields: Map[String, Seq[String]] =
    lines
      .map { line =>
        val Array(key, value) = line.split(":", 2)
        key.toLowerCase -> value.trim
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap

  def contentLength: Option[Int]  = fields.get("content-length").flatMap(_.headOption).flatMap(_.toIntOption)
  def contentType: Option[String] = fields.get("content-type").flatMap(_.headOption)
  def mimeType: Option[String]    = contentType.map(_.split(";")(0))
  def charset: Option[String]     = contentType.flatMap(_.split(";", 2).lift(1)).map(_.split("=", 2)(1))
  def cookies: Map[String, String] = {
    val list: Seq[String] = fields.get("cookie").getOrElse(Seq.empty)
    list
      .flatMap(_.split(";"))
      .map(_.trim)
      .map { line =>
        val Array(key, value) = line.split("=", 2)
        key -> value
      }
      .toMap
  }

  def asString = lines.mkString("\n")

}

object HttpRequestHeader {

  def fromString(lines: Seq[String]): HttpRequestHeader = HttpRequestHeader(lines)

}
