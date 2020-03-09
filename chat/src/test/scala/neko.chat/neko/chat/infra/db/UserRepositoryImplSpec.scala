package neko.chat.infra.db

import java.util.UUID
import java.time.Instant

import neko.core.jdbc.ConnectionIO
import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.entity.{User, RawPassword, Email, Auth}

import neko.chat.infra.db.share.TestDBPool
import org.scalatest._

class UserRepositoryImplSpec extends FunSuite with Matchers {

  def conn() = TestDBPool.getConnection()

  test("usersにinsert,selectができる") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z")
    val user   = User(userId, UserName("Foo"), now)

    val io: ConnectionIO[Option[User]] = for {
      _       <- UserRepositoryImpl.insertUserIO(user)
      userOpt <- UserRepositoryImpl.selectUserIO(userId)
    } yield userOpt

    val result = io.runRollback(conn())

    result shouldEqual Right(Option(user))
  }

  test("usersとauthsを追加してemail,hashedPasswordからuserIdを取得する") {
    val userId            = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now               = Instant.parse("2020-01-01T10:00:00.000Z")
    val user              = User(userId, UserName("Foo"), now)
    val rawPassword       = RawPassword("12345678")
    val applicationSecret = "dummy-salt-dummy-salt"
    val hashedPassword    = UserRepositoryImpl._createHashedPassword(rawPassword, applicationSecret)
    val email             = Email("dummy@example.com")
    val auth              = Auth(email, hashedPassword, userId)

    val io: ConnectionIO[Option[UserId]] = for {
      _       <- UserRepositoryImpl.insertUserIO(user)
      _       <- UserRepositoryImpl.insertAuthIO(auth)
      userOpt <- UserRepositoryImpl.selectUserIdFromAuthsIO(email, hashedPassword)
    } yield userOpt

    val result = io.runRollback(conn())

    result shouldEqual Right(Option(userId))
  }

  test("特定のuserのnameを変更出来る") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z")
    val user   = User(userId, UserName("Foo"), now)

    val io: ConnectionIO[Option[User]] = for {
      _       <- UserRepositoryImpl.insertUserIO(user)
      userOpt <- UserRepositoryImpl.selectUserIO(userId)
    } yield userOpt

    val result = io.runRollback(conn())

    result shouldEqual Right(Option(user))
  }

}
