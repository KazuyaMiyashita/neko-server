package neko.chat.repository

import org.scalatest._

import java.time.Clock

import neko.core.jdbc.ConnectionIO
import neko.chat.repository.share.TestDBPool
import neko.chat.entity.{User, Room, Message}

class MessageRepositoryImplSpec extends FlatSpec with Matchers {

  val userRepository: UserRepositoryImpl       = new UserRepositoryImpl(TestDBPool, Clock.systemUTC())
  val roomRepository: RoomRepositoryImpl       = new RoomRepositoryImpl(TestDBPool, Clock.systemUTC())
  val messageRepository: MessageRepositoryImpl = new MessageRepositoryImpl(TestDBPool, Clock.systemUTC())

  def conn() = TestDBPool.getConnection()

  private def genDataIO: ConnectionIO[(User, Room, List[Message])] =
    for {
      user <- userRepository._insert("Alice")
      room <- roomRepository._create("room01")
      mes1 <- messageRepository._post(room.id, user.id, "最初のメッセージ")
      mes2 <- messageRepository._post(room.id, user.id, "次のメッセージ")
      mes3 <- messageRepository._post(room.id, user.id, "みっつめのメッセージ")
    } yield (user, room, List(mes1, mes2, mes3))

  "MessageRepositoryImpl" should "postできる" in {
    val result: Either[Throwable, (User, Room, List[Message])] = genDataIO.runRollback(conn())
    result.isRight shouldEqual true
  }

  "MessageRepositoryImpl" should "fetchAllByRoomIdで投稿時間の降順で全件手に入る" in {
    val io = for {
      data <- genDataIO
      (user, room, List(mes1, mes2, mes3)) = data
      messages <- messageRepository._fetchAllByRoomId(room.id)
    } yield messages

    val messages: Either[Throwable, List[Message]] = io.runRollback(conn())
    messages.foreach { mess =>
      mess.foreach(println)
    }

    messages.map(_.map(_.message)) shouldEqual Right(
      List(
        "みっつめのメッセージ",
        "次のメッセージ",
        "最初のメッセージ"
      )
    )
  }

}
