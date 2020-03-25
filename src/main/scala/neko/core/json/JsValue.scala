package neko.core.json

trait JsValue {
  def \(key: String): JsPath
  def \(key: Int): JsPath
  final def as[T](implicit c: JsonDecoder[T]): Option[T] = c.decode(this)
}
case class JsString(value: String) extends JsValue {
  override def \(key: String): JsPath = JsPath(None)
  override def \(key: Int): JsPath    = JsPath(None)
}
case class JsNumber(value: Double) extends JsValue {
  override def \(key: String): JsPath = JsPath(None)
  override def \(key: Int): JsPath    = JsPath(None)
}
case class JsBoolean(value: Boolean) extends JsValue {
  override def \(key: String): JsPath = JsPath(None)
  override def \(key: Int): JsPath    = JsPath(None)
}
case class JsObject(value: Map[String, JsValue]) extends JsValue {
  override def \(key: String): JsPath = JsPath(value.get(key))
  override def \(key: Int): JsPath    = JsPath(None)
}
case class JsArray(value: Vector[JsValue]) extends JsValue {
  override def \(key: String): JsPath = JsPath(None)
  override def \(key: Int): JsPath    = JsPath(value.lift(key))
}
case object JsNull extends JsValue {
  override def \(key: String): JsPath = JsPath(None)
  override def \(key: Int): JsPath    = JsPath(None)
}

case class JsPath(getOption: Option[JsValue]) {
  def \(key: String): JsPath                             = JsPath(getOption.map(_ \ key).flatMap(_.getOption))
  def \(key: Int): JsPath                                = JsPath(getOption.map(_ \ key).flatMap(_.getOption))
  final def as[T](implicit c: JsonDecoder[T]): Option[T] = getOption.flatMap(c.decode(_))
}
