package neko.core.http

case class HttpRequestBody(
    private val bytes: Option[Array[Byte]]
) {

  def asString: String = bytes match {
    case Some(bs) => new String(bs, "UTF-8")
    case None     => ""
  }

}

object HttpRequestBody {

  def empty                         = HttpRequestBody(None)
  def fromBytes(bytes: Array[Byte]) = HttpRequestBody(Some(bytes))

}
