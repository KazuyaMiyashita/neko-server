package neko.server

object Main extends App {

  val routes = Routes(
    GET  -> "/"     -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/echo" -> (req => Response(OK, req.body)),
    POST -> "/json" -> JsonController.json
  )
  new Server(routes)

}

object JsonController {

  def json(request: Request): Response = {
    import neko.json.{Json, JsonDecoder, JsValue, JsObject}
    case class Person(name: String, age: Int)
    val requestDecoder: JsonDecoder[Person] = new JsonDecoder[Person] {
      override def decode(js: JsValue): Option[Person] = js match {
        case obj: JsObject =>
          for {
            name <- (obj \ "name").as[String]
            age  <- (obj \ "age").as[Int]
          } yield Person(name, age)
        case _ => None
      }
    }
    val personOpt: Option[Person] = Json.parse(request.body).toOption.flatMap(Json.decode(_)(requestDecoder))

    personOpt match {
      case None => Response(BAD_REQUEST)
      case Some(person) => {
        val json = Json.obj("message" -> Json.str(s"your name is ${person.name}, ${person.age} years old."))
        Response(OK, Json.format(json), "application/json")
      }
    }

  }

}
