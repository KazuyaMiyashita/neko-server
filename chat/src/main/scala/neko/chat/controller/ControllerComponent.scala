package neko.chat.controller

import neko.core.http.HttpResponseBuilder

class ControllerComponent(
    val responseBuilder: HttpResponseBuilder
)

object ControllerComponent {

  def create(allowOrigin: String): ControllerComponent = {

    val responseBuilder: HttpResponseBuilder = HttpResponseBuilder.default
      .withAllowControllAllowOrigin(allowOrigin)

    new ControllerComponent(responseBuilder)
  }

}
