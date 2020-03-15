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

  given StringDecoder as JsonDecoder[String] {
    def decode(js: JsValue): Option[String] = js match {
      case JsString(value) => Some(value)
      case _               => None
    }
  }

  given DoubleDecoder as JsonDecoder[Double] {
    def decode(js: JsValue): Option[Double] = js match {
      case JsNumber(value) => Some(value)
      case _               => None
    }
  }

  given IntDecoder as JsonDecoder[Int] {
    def decode(js: JsValue): Option[Int] = js match {
      case JsNumber(value) => Some(value.toInt)
      case _               => None
    }
  }

  given BooleanDecoder as JsonDecoder[Boolean] {
    def decode(js: JsValue): Option[Boolean] = js match {
      case JsBoolean(value) => Some(value)
      case _                => None
    }
  }

  given listDecoder (using U: JsonDecoder) as JsonDecoder[List[U]] {
    def decode(js: JsValue): Option[List[U]] = js match {
      // JsArrayの中の要素がUに揃っていなければLeft
      case JsArray(value) =>
        value
          .foldLeft[Option[List[U]]](Some(Nil)) { (acc, value) =>
            value.as[U].flatMap { u =>
              acc.map(u :: _)
            }
          }
          .map(_.reverse)
      case _ => None
    }
  }

}
