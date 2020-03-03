package neko.core.http

trait HttpApplication {

  def handle(request: HttpRequest): HttpResponse

}
