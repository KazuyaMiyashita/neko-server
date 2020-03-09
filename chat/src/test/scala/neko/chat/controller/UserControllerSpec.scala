package neko.chat.controller

import java.util.UUID
import java.time.Instant

import neko.chat.application.repository.UserRepository
import neko.chat.application.service.CreateUser
import neko.chat.application.service.CreateUser.{CreateUserRequest, DuplicateEmail}
import neko.chat.application.entity.{User, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}

import org.scalatest._

import neko.chat.ChatApplication
import neko.core.http.{HttpRequest, HttpRequestLine, HttpMethod, HttpRequestHeader, HttpRequestBody, HttpResponse, OK}
import neko.core.http.POST

class UserControllerSpec extends FlatSpec with Matchers {

  import UserControllerSpec._

  "POST /users" should "200" in {
    val stubCreateUser = new CreateUser {
      override def execute(request: CreateUserRequest): Either[CreateUser.CreateUserError, User] =
        Right(User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z")))
    }
    val userController = new UserController(fetchUserIdByToken = null, createUser = stubCreateUser, editUserInfo = null)
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null
    )

    val request = jsonRequest(POST, "/users", """{
        |  "name": "Foo",
        |  "email": "foo@example.com",
        |  "password": "abcde123"
        |}""".stripMargin)

    val exceptResponse = HttpResponse(OK)

    chatApplication.handle(request) shouldEqual exceptResponse
  }

}

object UserControllerSpec {

  def jsonRequest(method: HttpMethod, url: String, body: String): HttpRequest = {
    val contentLength = body.getBytes.length
    HttpRequest(
      HttpRequestLine(method, url, "HTTP/1.1"),
      HttpRequestHeader(Seq(s"Content-Length: $contentLength")),
      HttpRequestBody(Some(body.getBytes))
    )
  }

}
