package neko.chat

import scala.util.matching.Regex
import neko.core.http._

case class Router(routes: Route*) {

  def handle(request: HttpRequest): Option[HttpResponse] = {
    val handler: Option[HttpRequest => HttpResponse] = routes
      .find(route => route.method == request.line.method && route.url.matches(request.line.uri))
      .map(_.handler)
    handler.map(_.apply(request))
  }

}

case class Route(method: HttpMethod, url: Regex, handler: HttpRequest => HttpResponse)

case class RouteBuilder(method: HttpMethod, url: Regex) {
  def ->(handler: HttpRequest => HttpResponse) = Route(method, url, handler)
}

object RoutingDSL {

  implicit class HttpMethodDSL(self: HttpMethod) {
    def ->(url: String) = RouteBuilder(self, url.r)
    def ->(re: Regex)   = RouteBuilder(self, re)
  }

}
