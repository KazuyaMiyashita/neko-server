package neko.core.http

import scala.util.matching.Regex

case class Routes(routes: Route*) {

  def apply(request: Request): Response = {
    val handler: Option[Request => Response] = routes
      .find(route => route.method == request.header.method && route.url.matches(request.header.url))
      .map(_.handler)

    handler.map(_.apply(request)).getOrElse(Response(NOT_FOUND))
  }

}

case class Route(method: Method, url: Regex, handler: Request => Response)

case class RouteBuilder(method: Method, url: Regex) {
  def ->(handler: Request => Response) = Route(method, url, handler)
}
