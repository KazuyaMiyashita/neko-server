package neko.chat.controller

import java.util.UUID
import java.time.Instant

import neko.core.http.{HttpRequest, HttpRequestLine, HttpMethod, HttpRequestHeader, HttpRequestBody, OK}
import neko.core.http.{GET, POST}

import neko.chat.application.entity.{Message, Token}
import neko.chat.application.entity.Message.{MessageId, MessageBody}
import neko.chat.application.entity.User.{UserName, UserId}
import neko.chat.application.service.{FetchUserIdByToken, GetMessages, PostMessage}
import neko.chat.application.service.GetMessages.MessageResponse
import neko.chat.application.service.PostMessage.PostMessageRequest

import org.scalatest._

import neko.chat.ChatApplication

class MessageControllerSpec extends FlatSpec with Matchers {

  import MessageControllerSpec._

  "GET /messages" should "200" in {
    val stubGetMessages = new GetMessages {
      override def latest50messages(): List[MessageResponse] = List(
        MessageResponse(
          MessageId(UUID.fromString("64c9fa7e-93f9-483d-9508-25e582736882")),
          MessageBody("Hello2"),
          UserName("Bar"),
          Instant.parse("2020-01-02T00:00:00.000Z")
        ),
        MessageResponse(
          MessageId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2")),
          MessageBody("Hello"),
          UserName("Foo"),
          Instant.parse("2020-01-01T00:00:00.000Z")
        )
      )
    }
    val messageController =
      new MessageController(fetchUserIdByToken = null, getMessages = stubGetMessages, postMessage = null)
    val chatApplication =
      new ChatApplication(userController = null, authController = null, messageController = messageController)

    val request = buildJsonRequest(
      method = GET,
      url = "/messages",
      headers = Nil,
      body = ""
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
    response.body.get shouldEqual
      """[
        |  {
        |    "id": "64c9fa7e-93f9-483d-9508-25e582736882",
        |    "body": "Hello2",
        |    "userName": "Bar",
        |    "createdAt": 1577923200000
        |  },
        |  {
        |    "id": "53247465-de8c-47e8-ae01-d46d04db5dc2",
        |    "body": "Hello",
        |    "userName": "Foo",
        |    "createdAt": 1577836800000
        |  }
        |]""".stripMargin
  }

  "POST /messages" should "200" in {
    val stubFetchUserIdByToken = new FetchUserIdByToken {
      override def execute(token: Token): Option[UserId] = {
        Some(UserId(UUID.randomUUID()))
      }
    }
    val stubPostMessage = new PostMessage {
      override def execute(request: PostMessageRequest): Either[PostMessage.PostMessageError, Message] =
        Right(
          Message(
            MessageId(UUID.randomUUID()),
            UserId(UUID.randomUUID()),
            MessageBody("Hello"),
            Instant.parse("2020-01-01T22:00:00.000Z")
          )
        )

    }
    val messageController =
      new MessageController(
        fetchUserIdByToken = stubFetchUserIdByToken,
        getMessages = null,
        postMessage = stubPostMessage
      )
    val chatApplication =
      new ChatApplication(userController = null, authController = null, messageController = messageController)

    val request = buildJsonRequest(
      method = POST,
      url = "/messages",
      headers = "Cookie: token=dummy-token-dummy-token" :: Nil,
      body = """{
                |  "body": "Hello"
                |}""".stripMargin
    )

    val response = chatApplication.handle(request)

    response.status shouldEqual OK
  }

}

object MessageControllerSpec {

  def buildJsonRequest(method: HttpMethod, url: String, headers: List[String], body: String): HttpRequest = {
    val contentLength = body.getBytes.length
    HttpRequest(
      HttpRequestLine(method, url, "HTTP/1.1"),
      HttpRequestHeader(s"Content-Length: $contentLength" :: headers),
      HttpRequestBody(Some(body.getBytes))
    )
  }

}