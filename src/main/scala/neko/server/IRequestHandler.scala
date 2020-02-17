package neko.server

trait IRequestHandler {
  def handle(request: IRequest): Unit
}
