package neko.core.json

trait JsonEncoder[T] {
  def encode(value: T): JsValue
}

object JsonEncoder {

  given stringEncoder as JsonEncoder[String] {
    def encode(value: String): JsValue = JsString(value)
  }

  given doubleEncoder as JsonEncoder[Double] {
    def encode(value: Double): JsValue = JsNumber(value)
  }

  given intEncoder as JsonEncoder[Int] {
    def encode(value: Int): JsValue = JsNumber(value)
  }

  given booleanEncoder as JsonEncoder[Boolean] {
    def encode(value: Boolean): JsValue = JsBoolean(value)
  }

  given iterableEncoder (using U: JsonEncoder, Iter[U]: Iterable[U]) as JsonEncoder[Iter[U]] {
    def encode(value: Iter[U]): JsValue = JsArray(value.map(Json.encode(_)).toVector)
  }

  given nilEncoder as JsonEncoder[Nil.type] {
    def encode(value: Nil.type): JsValue = JsArray(Vector.empty)
  }

  given optionEncoder (using U: JsonEncoder, Opt[U]: Option[U]) as JsonEncoder[Opt[U]] {
    def encode(value: Opt[U]): JsValue = value match {
      case Some(v) => Json.encode(v)
      case None    => JsNull
    }
  }

  given noneEncoder as JsonEncoder[None.type] {
    def encode(value: None.type): JsValue = JsNull
  }

}
