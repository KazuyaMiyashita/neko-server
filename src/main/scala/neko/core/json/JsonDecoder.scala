package neko.core.json

trait JsonDecoder[T] {
  def decode(js: JsValue): Option[T]
  final def decodeOpt(js: JsValue): Option[Option[T]] =
    Some(js match {
      case JsNull => None
      case j      => decode(j)
    })
}

object JsonDecoder {

  implicit object StringDecoder extends JsonDecoder[String] {
    override def decode(js: JsValue): Option[String] = js match {
      case JsString(value) => Some(value)
      case _               => None
    }
  }

  implicit object DoubleDecoder extends JsonDecoder[Double] {
    override def decode(js: JsValue): Option[Double] = js match {
      case JsNumber(value) => Some(value)
      case _               => None
    }
  }

  implicit object IntDecoder extends JsonDecoder[Int] {
    override def decode(js: JsValue): Option[Int] = js match {
      case JsNumber(value) => Some(value.toInt)
      case _               => None
    }
  }

  implicit object BooleanDecoder extends JsonDecoder[Boolean] {
    override def decode(js: JsValue): Option[Boolean] = js match {
      case JsBoolean(value) => Some(value)
      case _                => None
    }
  }

}
