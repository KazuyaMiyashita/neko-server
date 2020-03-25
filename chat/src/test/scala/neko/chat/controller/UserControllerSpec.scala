package neko.chat.controller

import java.util.UUID
import java.time.Instant

import neko.core.http.{
  HttpRequest,
  HttpRequestLine,
  HttpMethod,
  HttpRequestHeader,
  HttpRequestBody,
  POST,
  OK,
  BAD_REQUEST
}

import neko.chat.application.entity.User
import neko.chat.application.entity.User.{UserId, UserName}

import neko.chat.application.usecase.CreateUser

import org.scalatest._

class UserControllerSpec extends FlatSpec with Matchers {

  import UserControllerSpec._

  val controllerConponent: ControllerComponent = ControllerComponent.create("http://localhost:8000")

  "POST /users" should "200" in {
    val stubCreateUser = new CreateUser(null) {
      override def execute(request: CreateUser.Request): Either[CreateUser.Error, User] =
        Right(User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z")))
    }
    val userController =
      new UserController(
        createUser = stubCreateUser,
        controllerConponent
      )
    val chatApplication = new Routing(
      userController = userController,
      authController = null,
      messageController = null,
      controllerConponent
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
      new UserController(
        createUser = stubCreateUser,
        controllerConponent
      )
    val chatApplication = new Routing(
      userController = userController,
      authController = null,
      messageController = null,
      controllerConponent
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
      new UserController(
        createUser = stubCreateUser,
        controllerConponent
      )
    val chatApplication = new Routing(
      userController = userController,
      authController = null,
      messageController = null,
      controllerConponent
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
