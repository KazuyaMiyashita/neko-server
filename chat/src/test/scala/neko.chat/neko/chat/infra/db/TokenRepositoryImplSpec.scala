package neko.chat.infra.db

import java.util.UUID
import java.time.{Clock, Instant, ZoneId}

import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Token

import neko.chat.infra.db.share.TestDBPool
import org.scalatest._

class TokenRepositoryImplSpec extends FunSuite with Matchers {

  val clock = Clock.fixed(Instant.parse("2020-01-01T10:00:00.000Z"), ZoneId.of("Z"))

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

}
