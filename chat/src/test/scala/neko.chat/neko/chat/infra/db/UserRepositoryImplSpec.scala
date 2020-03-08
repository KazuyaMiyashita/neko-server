// package neko.chat.infra.db

// import org.scalatest._

// import java.util.UUID
// import java.time.Instant

// import neko.chat.entity.User
// import neko.chat.repository.share.TestDBPool

// class UserRepositoryImplSpec extends FlatSpec with Matchers {

//   val userRepository: UserRepositoryImpl = new UserRepositoryImpl
//   def conn()                             = TestDBPool.getConnection()

//   "UserRepositoryImpl" should "insertできる" in {
//     val user = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))

//     val result: Either[Throwable, Unit] = userRepository.create(user).runRollback(conn())

//     result.swap.foreach(println) // 落ちた時に

//     result.isRight shouldEqual true
//   }

//   "UserRepositoryImpl" should "fetchByできる" in {
//     val user = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))

//     val io = for {
//       _    <- userRepository.create(user)
//       user <- userRepository.fetchBy(user.id)
//     } yield user
//     val result = io.runRollback(conn())

//     result.swap.foreach(println) // 落ちた時に

//     result shouldEqual Right(Some(user))
//   }

// }
