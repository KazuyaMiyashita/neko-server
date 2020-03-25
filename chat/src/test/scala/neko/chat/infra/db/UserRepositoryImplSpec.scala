package neko.chat.infra.db

import java.util.UUID
import java.time.Instant

import scala.util.{Try, Success}

import neko.core.jdbc.ConnectionIO
import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.entity.{User, RawPassword, HashedPassword, Email, Auth}
import neko.chat.application.repository.UserRepository

import neko.chat.infra.db.share.TestDBPool
import org.scalatest._

class UserRepositoryImplSpec extends FunSuite with Matchers {

  def conn() = TestDBPool.getConnection()

  test("UserRepositoryImpl._createHashedPasswordが良い感じにパスワードのハッシュ化をする") {

    val rawPasswords = List(
      RawPassword("abcde000"),
      RawPassword("abcde001"),
      RawPassword("abcde002"),
      RawPassword("abcde003"),
      RawPassword("abcde004")
    )
    val applicationSecret = "dummy-salt-dummy-salt"
    val hashedPasswords = rawPasswords.map { rawPassword =>
      UserRepositoryImpl._createHashedPassword(rawPassword, applicationSecret)
    }

    hashedPasswords shouldEqual List(
      HashedPassword("23DfBzvkN57Zt735u/ptz3YyTRaZGIIJAwUmMbd+BzdWcqoFh3Qqg4KGqFhdQTuuauBB23AdKEPTrLw0lvuCxA=="),
      HashedPassword("qP7qPKgJSgUDuOE8Ui5xQM5sneuNSIf/cH6yK2+d0GsfCPV54gFyuGXPvODY6Sj98/2vcsBe6FogSo0VHHyQiA=="),
      HashedPassword("nLt5ldpNgzzg/3Byml3J02sGwpBcgqomUEtwbZooWnAR2oogCbiwartorwd+F0ltCW/e9XaThtXues7fGmSXrA=="),
      HashedPassword("9VcKQAdTHYLJushvq/ZANDIQJsnwIOvQGfvdIXxmF9PPNx6GUy7R1E4A2Sd6e7g7qU4jksDXqhHRfY5nUaFOMA=="),
      HashedPassword("ta6s7fn0ylIgqJ2oIadLPhI/2LV9MRiZkdb8rp907ST6d8lhbMJgISdsfSXf14MRGxEiKlggGESaaME5X0rD0Q==")
    )

  }

  test("UserRepositoryImpl._createHashedPasswordがapplicationSecretを少しでも変えたら別のものになる") {

    val rawPassword = RawPassword("abcde000")

    val applicationSecrets = Seq(
      "dummy-salt-dummy-salt1",
      "dummy-salt-dummy-salt2",
      "dummy-salt-dummy-salt3",
      "dummy-salt-dummy-salt4",
      "dummy-salt-dummy-salt5"
    )
    val hashedPasswords = applicationSecrets.map { applicationSecret =>
      UserRepositoryImpl._createHashedPassword(rawPassword, applicationSecret)
    }

    hashedPasswords shouldEqual List(
      HashedPassword("lOpf6et/pqEAgeRaxWvsQ1Ij0YsLZYc4R40HZa5ScD92eJyr1UER9GNQWTWiHUKXFxjiXgOE83XUwcmNDqE+pA=="),
      HashedPassword("E2HFOrhIziAMWWF1cciJb9A3AWBHV/3AKRgC06+4HcsLQ3P9Nh6UAOSnaCqWXow5dzxHZdTOdKt3U7gQN8K1iA=="),
      HashedPassword("ANffm54DtQem3Th/Inw5Poc2j+ADdRGb1uWHAtom9GIkU6Lg/CjGMK87NJuAn8Tqc1xZm2Iokn07G7ZlubcvDw=="),
      HashedPassword("vP78X+1CU9nVwKhcVbplI+RHvIgMtPUqbka0viacUvlkLax2UMr02lueAMrEeLP7+jzVd5gsk3HNtePyxSCn9Q=="),
      HashedPassword("34o+OffAvDefkAgdLZ63YIw8e52yr8vmDS4rT60IE6fewUiqkdIXslnzXTA/u40OzHNXum/wsvB5doSosB2waA==")
    )

  }

  test("usersにinsert,selectができる") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z")
    val user   = User(userId, UserName("Foo"), now)

    val io: ConnectionIO[Nothing, Option[User]] = for {
      _       <- UserRepositoryImpl.insertUserIO(user)
      userOpt <- UserRepositoryImpl.selectUserIO(userId)
    } yield userOpt

    val result: Try[Either[Nothing, Option[User]]] = io.runRollback(conn())

    result shouldEqual Success(Right(Some(user)))
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

    val io: ConnectionIO[Any, Option[UserId]] = for {
      _       <- UserRepositoryImpl.insertUserIO(user)
      _       <- UserRepositoryImpl.insertAuthIO(auth)
      userOpt <- UserRepositoryImpl.selectUserIdFromAuthsIO(email, hashedPassword)
    } yield userOpt

    val result: Try[Either[Any, Option[User.UserId]]] = io.runRollback(conn())

    result shouldEqual Success(Right(Some(userId)))
  }

  test("authsのemailは重複できない") {
    val email = Email("dummy@example.com")
    val user1 = User(UserId(UUID.randomUUID()), UserName("Foo"), Instant.parse("2020-01-01T10:00:00.000Z"))
    val auth1 = Auth(email, HashedPassword("dummy-dummy-dummy"), user1.id)
    val user2 = User(UserId(UUID.randomUUID()), UserName("Bar"), Instant.parse("2020-01-01T10:00:00.000Z"))
    val auth2 = Auth(email, HashedPassword("dummy-dummy-dummy"), user2.id)

    val io: ConnectionIO[UserRepository.SaveNewUserError, Unit] = for {
      _ <- UserRepositoryImpl.insertUserIO(user1)
      _ <- UserRepositoryImpl.insertAuthIO(auth1)
      _ <- UserRepositoryImpl.insertUserIO(user2)
      _ <- UserRepositoryImpl.insertAuthIO(auth2)
    } yield ()

    val result: Try[Either[UserRepository.SaveNewUserError, Unit]] = io.runRollback(conn())

    result.get.swap.getOrElse(throw new Exception) shouldBe a[UserRepository.SaveNewUserError.DuplicateEmail]
  }

}
