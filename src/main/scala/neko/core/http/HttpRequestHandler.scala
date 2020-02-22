package neko.core.http

import neko.core.server.{IRequest, IRequestHandler}
import java.io.{BufferedWriter, OutputStreamWriter}

class HttpRequestHandler(routes: Routes) extends IRequestHandler {

  override def handle(req: IRequest): Unit = {
    val httpRequest: HttpRequest = HttpRequest.fromInputStream(req.in)
    println("**request**")
    println(httpRequest.asString)

    val httpResponse = routes(httpRequest)

    val out = new BufferedWriter(new OutputStreamWriter(req.out))
    out.write(httpResponse.asString)
    out.flush()

    println("**response**")
    println(httpResponse.asString)

    req.close()

  }

}
