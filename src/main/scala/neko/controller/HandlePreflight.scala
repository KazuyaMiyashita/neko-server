package neko.controller

import neko.server.Request
import neko.server.OK
import neko.server.Response

object HandlePreflight {

  def apply(request: Request) = {
    println(request)
    Response(OK)
      .withHeader("Access-Control-Allow-Origin", "*")
      .withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
      .withHeader("Access-Control-Allow-Headers", "*")
      .withHeader("Access-Control-Max-Age", "1728000")
      .withHeader("Content-Type", "text/plain")
  }

}
