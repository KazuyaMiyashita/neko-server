package neko.chat.controller.common

import neko.core.http.HttpResponseBuilder

class HttpResponseBuilderFactory(
    allowOrigin: String
) {

  val responseBuilder = HttpResponseBuilder.default
    .withAllowControllAllowOrigin(allowOrigin)

}
