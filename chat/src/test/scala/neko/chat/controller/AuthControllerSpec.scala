package neko.chat.controller

import java.util.UUID

import neko.chat.ChatApplication
import neko.core.http.{HttpRequest, HttpRequestLine, HttpMethod, HttpRequestHeader, HttpRequestBody, OK}
import neko.core.http.{GET, POST}

import neko.chat.application.usecase.{FetchUserIdByToken, Login, Logout}
import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId

import org.scalatest._

class AuthControllerSpec extends FlatSpec with Matchers {

  import AuthControllerSpec._

  "POST /auth/login" should "200" in {
    val stubLogin = new Login(null, null) {
      override def execute(request: Login.Request): Either[Login.Error, Token] =
        Right(Token("stub-token-stub-token"))
    }
    val authController = new AuthController(
      fetchUserIdByToken = null,
      login = stubLogin,
      logout = null
    )
    val chatApplication = new ChatApplication(
      userController = null,
      authController = authController,
      messageController = null
    )

    val request = buildJsonRequest(
      method = POST,
      url = "/auth/login",
      headers = Nil,
      body = """{
               |  "email": "foo@example.com",
               |  "password": "abcde123"
               |}""".stripMargin
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
    response.headers.get("Set-Cookie").get shouldEqual "token=stub-token-stub-token; Path=/"
  }

  "POST /auth/logout" should "200" in {
    val stubLogout = new Logout(null) {
      override def execute(token: Token): Boolean = true
    }
    val authController = new AuthController(
      fetchUserIdByToken = null,
      login = null,
      logout = stubLogout
    )
    val chatApplication = new ChatApplication(
      userController = null,
      authController = authController,
      messageController = null
    )

    val request = buildJsonRequest(
      method = POST,
      url = "/auth/logout",
      headers = "Cookie: token=dummy-token-dummy-token" :: Nil,
      body = ""
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
    response.headers.get("Set-Cookie").get shouldEqual "token=; Path=/; Max-Age=0"
  }

  "POST /auth/session" should "200" in {
    val stubFetchUserIdByToken = new FetchUserIdByToken(null) {
      override def execute(token: Token): Option[UserId] = {
        Some(UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2")))
      }
    }
    val authController = new AuthController(
      fetchUserIdByToken = stubFetchUserIdByToken,
      login = null,
      logout = null
    )
    val chatApplication = new ChatApplication(
      userController = null,
      authController = authController,
      messageController = null
    )

    val request = buildJsonRequest(
      method = GET,
      url = "/auth/session",
      headers = "Cookie: token=dummy-token-dummy-token" :: Nil,
      body = ""
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
    response.body.get shouldEqual
      """{
        |  "userId": "53247465-de8c-47e8-ae01-d46d04db5dc2"
        |}""".stripMargin
  }

}

object AuthControllerSpec {

  def buildJsonRequest(method: HttpMethod, url: String, headers: List[String], body: String): HttpRequest = {
    val contentLength = body.getBytes.length
    HttpRequest(
      HttpRequestLine(method, url, "HTTP/1.1"),
      HttpRequestHeader(s"Content-Length: $contentLength" :: headers),
      HttpRequestBody(Some(body.getBytes))
    )
  }

}
