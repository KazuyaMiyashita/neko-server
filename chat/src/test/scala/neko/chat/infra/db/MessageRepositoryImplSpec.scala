package neko.chat.infra.db

import java.util.UUID
import java.time.Instant

import scala.util.{Try, Success}

import neko.core.jdbc.ConnectionIO

import neko.chat.application.entity.{User, Message}
import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.entity.Message.{MessageId, MessageBody}
import neko.chat.application.repository.MessageRepository.MessageResponse

import neko.chat.infra.db.share.TestDBPool
import org.scalatest._

class MessageRepositoryImplSpec extends FunSuite with Matchers {

  def conn() = TestDBPool.getConnection()

  test("メッセージを投稿できる") {

    val user = User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z"))
    val message =
      Message(MessageId(UUID.randomUUID()), user.id, MessageBody("Hello"), Instant.parse("2020-01-01T22:00:00.000Z"))

    val io: ConnectionIO[Nothing, Unit] = for {
      _ <- UserRepositoryImpl.insertUserIO(user)
      _ <- MessageRepositoryImpl.saveMessageIO(message)
    } yield ()

    val result: Try[Either[Nothing, Unit]] = io.runRollback(conn())

    result shouldEqual Success(Right(()))

  }

  test("最新のメッセージを最新順に最大50件取得できる") {

    val users = Vector(
      User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z")),
      User(UserId(UUID.randomUUID()), UserName("Bar"), Instant.parse("2020-01-01T10:00:00.000Z"))
    )

    val messageResponses = for (n <- 0 until 100) yield {
      val user = users(n % users.size)
      val message = Message(
        MessageId(UUID.randomUUID()),
        user.id,
        MessageBody("Hello" + n.toString),
        Instant.parse("2020-01-01T10:00:00.000Z").plusSeconds(n)
      )
      MessageResponse(message, user)
    }
    val messages = messageResponses.map { case MessageResponse(message, user) => message }

    val io: ConnectionIO[Nothing, List[MessageResponse]] = for {
      _                <- ConnectionIO.sequence(users.map(user => UserRepositoryImpl.insertUserIO(user)))
      _                <- ConnectionIO.sequence(messages.map(message => MessageRepositoryImpl.saveMessageIO(message)))
      messageResponses <- MessageRepositoryImpl.fetchLatest50messagesIO()
    } yield messageResponses

    val result: Try[Either[Nothing, List[MessageResponse]]] = io.runRollback(conn())

    result shouldEqual Success(Right(messageResponses.reverse.take(50)))

  }

}
