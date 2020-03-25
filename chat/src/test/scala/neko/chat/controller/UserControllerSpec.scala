package neko.chat.controller

import java.util.UUID
import java.time.Instant

import neko.core.http.{HttpRequest, HttpRequestLine, HttpMethod, HttpRequestHeader, HttpRequestBody, OK, BAD_REQUEST}
import neko.core.http.{POST, PUT}

import neko.core.http.HttpResponseBuilder
import neko.chat.controller.common.HttpResponseBuilderFactory

import neko.chat.application.entity.{User, Token}
import neko.chat.application.entity.User.{UserId, UserName}

import neko.chat.application.usecase.{CreateUser, FetchUserIdByToken, EditUserInfo}

import neko.chat.ChatApplication

import org.scalatest._

class UserControllerSpec extends FlatSpec with Matchers {

  import UserControllerSpec._

  val responseBuilder: HttpResponseBuilder = new HttpResponseBuilderFactory("http://localhost:8000").responseBuilder

  "POST /users" should "200" in {
    val stubCreateUser = new CreateUser(null) {
      override def execute(request: CreateUser.Request): Either[CreateUser.Error, User] =
        Right(User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z")))
    }
    val userController =
      new UserController(fetchUserIdByToken = null, createUser = stubCreateUser, editUserInfo = null, responseBuilder)
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null,
      responseBuilder
    )

    val request = buildJsonRequest(
      method = POST,
      url = "/users",
      headers = Nil,
      body = """{
               |  "name": "Foo",
               |  "email": "foo@example.com",
               |  "password": "abcde123"
               |}""".stripMargin
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
  }

  "POST /users" should "400 (one error)" in {
    val stubCreateUser = new CreateUser(null) {
      override def execute(request: CreateUser.Request): Either[CreateUser.Error, User] =
        Left(CreateUser.Error.ValidateErrors(CreateUser.ValidateError.UserNameTooLong))
    }
    val userController =
      new UserController(fetchUserIdByToken = null, createUser = stubCreateUser, editUserInfo = null, responseBuilder)
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null,
      responseBuilder
    )

    val request = buildJsonRequest(
      method = POST,
      url = "/users",
      headers = Nil,
      body = """{
               |  "name": "FooooOooooOooooOoooo",
               |  "email": "foo@example.com",
               |  "password": "abcde123"
               |}""".stripMargin // This test does not actually verify this here
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual BAD_REQUEST
    response.body.get shouldEqual
      """{
        |  "errors": {
        |    "name": "ユーザー名は20文字以下である必要があります"
        |  }
        |}""".stripMargin
  }

  "POST /users" should "400 (three errors)" in {
    import CreateUser.ValidateError._

    val stubCreateUser = new CreateUser(null) {
      override def execute(request: CreateUser.Request): Either[CreateUser.Error, User] =
        Left(CreateUser.Error.ValidateErrors(UserNameTooLong, EmailWrongFormat, RawPasswordTooShort))
    }
    val userController =
      new UserController(fetchUserIdByToken = null, createUser = stubCreateUser, editUserInfo = null, responseBuilder)
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null,
      responseBuilder
    )

    val request = buildJsonRequest(
      method = POST,
      url = "/users",
      headers = Nil,
      body = """{
               |  "name": "FooooOooooOooooOoooo",
               |  "email": "foo-example.com",
               |  "password": "pass"
               |}""".stripMargin // This test does not actually verify this here
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual BAD_REQUEST
    response.body.get shouldEqual
      """{
        |  "errors": {
        |    "name": "ユーザー名は20文字以下である必要があります",
        |    "email": "メールアドレスの形式がおかしい",
        |    "password": "パスワードは8文字以上である必要があります"
        |  }
        |}""".stripMargin
  }

  "POST /edit" should "200" in {
    val stubFetchUserIdByToken = new FetchUserIdByToken(null) {
      override def execute(token: Token): Option[UserId] = {
        Some(UserId(UUID.randomUUID()))
      }
    }
    val stubEditUserInfo = new EditUserInfo(null) {
      override def execute(request: EditUserInfo.Request): Either[EditUserInfo.Error, Unit] = {
        Right(())
      }
    }
    val userController = new UserController(
      fetchUserIdByToken = stubFetchUserIdByToken,
      createUser = null,
      editUserInfo = stubEditUserInfo,
      responseBuilder
    )
    val chatApplication = new ChatApplication(
      userController = userController,
      authController = null,
      messageController = null,
      responseBuilder
    )

    val request = buildJsonRequest(
      method = PUT,
      url = "/users",
      headers = "Cookie: token=dummy-token-dummy-token" :: Nil,
      body = """{
                |  "name": "Bar"
                |}""".stripMargin
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
  }

}

object UserControllerSpec {

  def buildJsonRequest(method: HttpMethod, url: String, headers: List[String], body: String): HttpRequest = {
    val contentLength = body.getBytes.length
    HttpRequest(
      HttpRequestLine(method, url, "HTTP/1.1"),
      HttpRequestHeader(s"Content-Length: $contentLength" :: headers),
      HttpRequestBody(Some(body.getBytes))
    )
  }

}
