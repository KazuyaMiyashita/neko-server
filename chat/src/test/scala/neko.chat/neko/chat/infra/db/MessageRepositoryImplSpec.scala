// package neko.chat.infra.db

// import org.scalatest._

// import java.util.UUID
// import java.time.Instant

// import neko.core.jdbc.ConnectionIO
// import neko.chat.repository.share.TestDBPool
// import neko.chat.entity.{User, Message}
// import neko.chat.repository.MessageRepository.MessageResponse

// class MessageRepositoryImplSpec extends FlatSpec with Matchers {

//   val userRepository: UserRepositoryImpl       = new UserRepositoryImpl
//   val messageRepository: MessageRepositoryImpl = new MessageRepositoryImpl

//   def conn() = TestDBPool.getConnection()

//   object dummy {
//     val user = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))
//     val messages: List[Message] = List(
//       Message(UUID.randomUUID(), user.id, "最初のメッセージ", Instant.parse("2020-01-01T10:00:00.001Z")),
//       Message(UUID.randomUUID(), user.id, "次のメッセージ", Instant.parse("2020-01-01T10:00:00.002Z")),
//       Message(UUID.randomUUID(), user.id, "みっつめのメッセージ", Instant.parse("2020-01-01T10:00:00.003Z"))
//     )

//     def genDataIO: ConnectionIO[Unit] =
//       for {
//         _ <- userRepository.create(user)
//         _ <- ConnectionIO.sequence(messages.map(messageRepository.post))
//       } yield ()
//   }

//   "MessageRepositoryImpl" should "postできる" in {
//     val result: Either[Throwable, Unit] = dummy.genDataIO.runRollback(conn())
//     result.isRight shouldEqual true
//   }

//   "MessageRepositoryImpl" should "fetch(投稿時間の降順で50件まで手に入る)" in {
//     val io = for {
//       _        <- dummy.genDataIO
//       messages <- messageRepository.get()
//     } yield messages

//     val result: Either[Throwable, List[MessageResponse]] = io.runRollback(conn())
//     val answer = Right(
//       dummy.messages
//         .map({ m =>
//           MessageResponse(m, dummy.user)
//         })
//         .reverse
//     )

//     result shouldEqual answer
//   }

// }
