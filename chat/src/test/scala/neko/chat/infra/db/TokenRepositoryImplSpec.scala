package neko.chat.infra.db

import java.util.UUID
import java.time.Instant
import java.sql.SQLIntegrityConstraintViolationException

import scala.util.{Try, Success}

import neko.core.jdbc.ConnectionIO
import neko.chat.application.entity.User.{UserId, UserName}
import neko.chat.application.entity.{User, Token}

import neko.chat.infra.db.share.TestDBPool
import org.scalatest._

class TokenRepositoryImplSpec extends FunSuite with Matchers {

  def conn() = TestDBPool.getConnection()

  test("TokenRepositoryImpl._createTokenが良い感じにトークンを生成する") {
    val userIds = List(
      UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2")),
      UserId(UUID.fromString("64c9fa7e-93f9-483d-9508-25e582736882")),
      UserId(UUID.fromString("1f7c110f-2ef7-4f81-b99f-04f54f4b3f7e")),
      UserId(UUID.fromString("dfce2026-5e2e-42a3-a41e-0b9bc68927d1")),
      UserId(UUID.fromString("9a602bf7-611a-41c2-9e5f-aa3805d06fdc"))
    )
    val now    = Instant.parse("2020-01-01T10:00:00.000Z").toEpochMilli
    val salt   = "dummy-salt-dummy-salt"
    val tokens = userIds.map(userId => TokenRepositoryImpl._createToken(userId, now, salt))

    tokens shouldEqual List(
      Token("SFbvQ9hIhbrge0dBG~6yVUYnDNqCX4~cOwhLUHAgq9TahjgJOHTuLCMkPPT0CQAG"),
      Token("4h+iX.1eEn9gY1+cARcJDpsLYMOU5TlfKhXQ5yD7vAe5ndqVO5Gxn1-olGhE0wOR"),
      Token("/nxtU.6uJADNIfs5qjBHzQoFHgRiLYcEms8mIJs4Uvn_uXi.j1IhRjuOkLhu.ENn"),
      Token("EtBGHsH5gb0HLJZvg7JbILIRkxFqfmSTn6_CYMTtsGXykag0Ygdta~rhxGSedLc2"),
      Token("TkwSQMT8oo9.cmIq2V7EKB8vejNIOhWz6sDkhDWgebBqiwG9q/7yWA5gXwQoqc2U")
    )
  }

  test("TokenRepositoryImpl._createTokenがnowを少しでも変えたら別のになる") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now1   = Instant.parse("2020-01-01T10:00:00.000Z").toEpochMilli
    val now2   = Instant.parse("2020-01-01T10:00:00.001Z").toEpochMilli
    val salt   = "dummy-salt-dummy-salt"
    val token1 = TokenRepositoryImpl._createToken(userId, now1, salt)
    val token2 = TokenRepositoryImpl._createToken(userId, now2, salt)

    token1 should not equal token2
  }

  test("TokenRepositoryImpl._createTokenがsaltを少しでも変えたら別のになる") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z").toEpochMilli
    val salt1  = "dummy-salt-dummy-salt"
    val salt2  = "dummy-salt-dummy-salt "
    val token1 = TokenRepositoryImpl._createToken(userId, now, salt1)
    val token2 = TokenRepositoryImpl._createToken(userId, now, salt2)

    token1 should not equal token2
  }

  test("usersに該当のidが存在する時、tokenをDBに追加できる") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z")
    val user   = User(userId, UserName("Foo"), now)

    val salt  = "dummy-salt-dummy-salt"
    val token = TokenRepositoryImpl._createToken(userId, now.toEpochMilli, salt)

    val io: ConnectionIO[Nothing, Unit] = for {
      _ <- UserRepositoryImpl.insertUserIO(user)
      _ <- TokenRepositoryImpl.insertTokenIO(userId, token, now)
    } yield ()
    val result: Try[Either[Nothing, Unit]] = io.runRollback(conn())

    result shouldEqual Success(Right(()))
  }

  test("usersに該当のidが存在しない時は、tokenはDBに追加できない") {
    val userId = UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2"))
    val now    = Instant.parse("2020-01-01T10:00:00.000Z")

    val salt  = "dummy-salt-dummy-salt"
    val token = TokenRepositoryImpl._createToken(userId, now.toEpochMilli, salt)

    val io: ConnectionIO[Nothing, Unit] = for {
      _ <- TokenRepositoryImpl.insertTokenIO(userId, token, now)
    } yield ()
    val result: Try[Either[Nothing, Unit]] = io.runRollback(conn())

    result.toEither.swap.getOrElse(throw new Exception) shouldBe a[SQLIntegrityConstraintViolationException]
  }

  test("tokenから特定のUserIdを取得できる") {
    val userIds = List(
      UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2")),
      UserId(UUID.fromString("64c9fa7e-93f9-483d-9508-25e582736882")),
      UserId(UUID.fromString("1f7c110f-2ef7-4f81-b99f-04f54f4b3f7e")),
      UserId(UUID.fromString("dfce2026-5e2e-42a3-a41e-0b9bc68927d1")),
      UserId(UUID.fromString("9a602bf7-611a-41c2-9e5f-aa3805d06fdc"))
    )
    val now   = Instant.parse("2020-01-01T10:00:00.000Z")
    val users = userIds.map(userId => User(userId, UserName("Foo"), now))

    val salt   = "dummy-salt-dummy-salt"
    val tokens = users.map(user => TokenRepositoryImpl._createToken(user.id, now.toEpochMilli, salt))

    val targetUserId = userIds(3)
    val targetToken  = tokens(3)

    val io: ConnectionIO[Nothing, Option[UserId]] = for {
      _ <- ConnectionIO.sequence(users.map(user => UserRepositoryImpl.insertUserIO(user)))
      _ <- ConnectionIO.sequence {
        (users zip tokens).map {
          case (user, token) =>
            TokenRepositoryImpl.insertTokenIO(user.id, token, now)
        }
      }
      userId <- TokenRepositoryImpl.fetchUserIdByTokenIO(targetToken)
    } yield userId

    val result: Try[Either[Nothing, Option[UserId]]] = io.runRollback(conn())

    result shouldEqual Success(Right(Some(targetUserId)))
  }

  test("指定したtokenのみ削除できる") {
    val userIds = List(
      UserId(UUID.fromString("53247465-de8c-47e8-ae01-d46d04db5dc2")),
      UserId(UUID.fromString("64c9fa7e-93f9-483d-9508-25e582736882")),
      UserId(UUID.fromString("1f7c110f-2ef7-4f81-b99f-04f54f4b3f7e")),
      UserId(UUID.fromString("dfce2026-5e2e-42a3-a41e-0b9bc68927d1")),
      UserId(UUID.fromString("9a602bf7-611a-41c2-9e5f-aa3805d06fdc"))
    )
    val now   = Instant.parse("2020-01-01T10:00:00.000Z")
    val users = userIds.map(userId => User(userId, UserName("Foo"), now))

    val salt   = "dummy-salt-dummy-salt"
    val tokens = users.map(user => TokenRepositoryImpl._createToken(user.id, now.toEpochMilli, salt))

    val targetUserId = userIds(3)
    val targetToken  = tokens(3)

    val io: ConnectionIO[Nothing, (List[Option[UserId]], Boolean, List[Option[UserId]])] = for {
      _ <- ConnectionIO.sequence(users.map(user => UserRepositoryImpl.insertUserIO(user)))
      _ <- ConnectionIO.sequence {
        (users zip tokens).map {
          case (user, token) =>
            TokenRepositoryImpl.insertTokenIO(user.id, token, now)
        }
      }
      before    <- ConnectionIO.sequence(tokens.map(token => TokenRepositoryImpl.fetchUserIdByTokenIO(token)))
      isDeleted <- TokenRepositoryImpl.deleteTokenIO(targetToken)
      after     <- ConnectionIO.sequence(tokens.map(token => TokenRepositoryImpl.fetchUserIdByTokenIO(token)))
    } yield (before.toList, isDeleted, after.toList)

    val result: Try[Either[Throwable, (List[Option[UserId]], Boolean, List[Option[UserId]])]] = io.runRollback(conn())
    val (before, isDeleted, after)                                                            = result.get.getOrElse(throw new Exception)

    before shouldEqual userIds.map(Some(_))
    isDeleted shouldEqual true
    after shouldEqual userIds.map {
      case `targetUserId` => None
      case x              => Some(x)
    }
  }

}
