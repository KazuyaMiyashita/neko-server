package neko.chat.controller

import java.util.UUID
import java.time.Instant

import neko.chat.application.service.{CreateUser, FetchUserIdByToken, EditUserInfo}
import neko.chat.application.service.CreateUser.CreateUserRequest
import neko.chat.application.entity.{User, Token}
import neko.chat.application.entity.User.{UserId, UserName}

import org.scalatest._

import neko.chat.ChatApplication
import neko.core.http.{HttpRequest, HttpRequestLine, HttpMethod, HttpRequestHeader, HttpRequestBody, HttpResponse, OK}
import neko.core.http.{POST, PUT}
import neko.chat.application.service.FetchUserIdByToken

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

    val request = buildJsonRequest(POST, "/users")()("""{
        |  "name": "Foo",
        |  "email": "foo@example.com",
        |  "password": "abcde123"
        |}""".stripMargin)

    val exceptResponse = HttpResponse(OK)

    chatApplication.handle(request) shouldEqual exceptResponse
  }

  "POST /edit" should "200" in {
    val stubFetchUserIdByToken = new FetchUserIdByToken {
      override def execute(token: Token): Option[UserId] = {
        Some(UserId(UUID.randomUUID()))
      }
    }
    val stubEditUserInfo = new EditUserInfo {
      override def execute(userId: UserId, newUserNameStr: String): Either[EditUserInfo.EditUserInfoError, Unit] = {
        Right(())
      }
    }
    val userController = new UserController(
      fetchUserIdByToken = stubFetchUserIdByToken,
      createUser = null,
      editUserInfo = stubEditUserInfo
    )
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null
    )

    val request = buildJsonRequest(PUT, "/users")(
      "Cookie: token=dummy-token-dummy-token"
    )("""{
        |  "name": "Bar"
        |}""".stripMargin)

    val exceptResponse = HttpResponse(OK)

    chatApplication.handle(request) shouldEqual exceptResponse
  }

}

object UserControllerSpec {

  def buildJsonRequest(method: HttpMethod, url: String)(headers: String*)(body: String): HttpRequest = {
    val contentLength = body.getBytes.length
    HttpRequest(
      HttpRequestLine(method, url, "HTTP/1.1"),
      HttpRequestHeader(s"Content-Length: $contentLength" +: headers),
      HttpRequestBody(Some(body.getBytes))
    )
  }

}
