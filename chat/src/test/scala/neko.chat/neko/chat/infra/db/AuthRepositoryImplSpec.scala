// package neko.chat.infra.db

// import org.scalatest._

// import java.util.UUID
// import java.time.{Clock, Instant, ZoneId}

// import neko.chat.repository.share.TestDBPool
// import neko.chat.entity.{User, Auth}
// import neko.chat.auth.Token

// import neko.chat.repository.AuthRepository.UserNotExistOrDuplicateUserNameException
// import neko.core.jdbc.ConnectionIO

// class AuthRepositoryImplSpec extends FlatSpec with Matchers {

//   val clock                              = Clock.fixed(Instant.parse("2020-01-01T10:00:00.000Z"), ZoneId.of("Z"))
//   val userRepository: UserRepositoryImpl = new UserRepositoryImpl
//   val authRepository: AuthRepositoryImpl = new AuthRepositoryImpl(clock)

//   def conn() = TestDBPool.getConnection()

//   "AuthRepositoryImpl" should "createできる" in {
//     val user = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))
//     val auth = Auth("alice@example.com", "hashedPassword", user.id)
//     val io = for {
//       _ <- userRepository.create(user)
//       _ <- authRepository.create(auth)
//     } yield ()
//     val result = io.runRollback(conn())

//     assert(result.isRight)
//   }

//   "AuthRepositoryImpl" should "ユーザーが存在しないとcreateできない" in {
//     val auth = Auth("alice@example.com", "hashedPassword", UUID.randomUUID())
//     val io = for {
//       _ <- authRepository.create(auth)
//     } yield ()
//     val result = io.runRollback(conn())

//     assert(result.swap.getOrElse(throw new Exception).isInstanceOf[UserNotExistOrDuplicateUserNameException])
//   }

//   "AuthRepositoryImpl" should "loginNameが重複してるとcreateできない" in {

//     val user  = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))
//     val auth  = Auth("LOGINNAME", "hashedPassword", user.id)
//     val user2 = User(UUID.randomUUID(), "Bob", Instant.parse("2020-01-01T10:00:00.000Z"))
//     val auth2 = Auth("LOGINNAME", "hashedPassword2", user2.id)
//     val io = for {
//       _ <- userRepository.create(user)
//       _ <- authRepository.create(auth)
//       _ <- userRepository.create(user2)
//       _ <- authRepository.create(auth2)
//     } yield ()
//     val result = io.runRollback(conn())

//     assert(result.swap.getOrElse(throw new Exception).isInstanceOf[UserNotExistOrDuplicateUserNameException])

//   }

//   "AuthRepositoryImpl" should "create-login-authenticate-logoutできる" in {

//     val loginName      = "alice@example.com"
//     val rawPassword    = "mypassword"
//     val hashedPassword = AuthRepository.generateHashedPassword(rawPassword, loginName)
//     val user           = User(UUID.randomUUID(), "Alice", Instant.parse("2020-01-01T10:00:00.000Z"))
//     val auth           = Auth(loginName, hashedPassword, user.id)
//     val io: ConnectionIO[(Token, User)] = for {
//       _         <- userRepository.create(user)
//       _         <- authRepository.create(auth)
//       token     <- authRepository.login(loginName, rawPassword).map(_.get)
//       loginUser <- authRepository.authenticate(token).map(_.get)
//       _         <- authRepository.logout(token)
//     } yield (token, loginUser)
//     val result = io.runRollback(conn())

//     result match {
//       case Left(e) => throw e
//       case Right((_, loginUser)) => {
//         loginUser shouldEqual user
//       }
//     }

//   }

// }
