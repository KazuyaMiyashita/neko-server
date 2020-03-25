package neko.core.json

trait JsonEncoder[T] {
  def encode(value: T): JsValue
}

object JsonEncoder {

  def encode[T](value: T)(implicit encoder: JsonEncoder[T]): JsValue = encoder.encode(value)

  implicit object StringEncoder extends JsonEncoder[String] {
    override def encode(value: String): JsValue = JsString(value)
  }

  implicit object DoubleEncoder extends JsonEncoder[Double] {
    override def encode(value: Double): JsValue = JsNumber(value)
  }

  implicit object IntEncoder extends JsonEncoder[Int] {
    override def encode(value: Int): JsValue = JsNumber(value)
  }

  implicit object BooleanEncoder extends JsonEncoder[Boolean] {
    override def encode(value: Boolean): JsValue = JsBoolean(value)
  }

  implicit def iterableEncoder[U, Iter[U] <: Iterable[U]](implicit encoder: JsonEncoder[U]) = new JsonEncoder[Iter[U]] {
    override def encode(value: Iter[U]): JsValue = JsArray(value.map(encoder.encode(_)).toVector)
  }

  implicit object NilEncoder extends JsonEncoder[Nil.type] {
    override def encode(value: Nil.type): JsValue = JsArray(Vector.empty)
  }

  implicit def optionEncoder[U, Opt[U] <: Option[U]](implicit encoder: JsonEncoder[U]) = new JsonEncoder[Opt[U]] {
    override def encode(value: Opt[U]): JsValue = value match {
      case Some(v) => encoder.encode(v)
      case None    => JsNull
    }
  }

  implicit object NoneEncoder extends JsonEncoder[None.type] {
    override def encode(value: None.type): JsValue = JsNull
  }

  implicit def mapEncoder[U](implicit encoder: JsonEncoder[U]) = new JsonEncoder[Map[String, U]] {
    override def encode(value: Map[String, U]): JsValue =
      JsObject(value.map { case (str, js) => str -> encoder.encode(js) })
  }

}
