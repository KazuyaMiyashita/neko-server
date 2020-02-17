package neko.core.server

trait IRequestHandler {
  def handle(request: IRequest): Unit
}
