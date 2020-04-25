package neko.chat.infra.db

import java.util.UUID
import java.time.Clock
import java.sql.{ResultSet, Timestamp}

import scala.util.Random

import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._

import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId
import neko.chat.application.repository.TokenRepository

class TokenRepositoryImpl(
    clock: Clock,
    applicationSecret: String
) extends TokenRepository {

  import TokenRepositoryImpl._

  override def createToken(userId: UserId): Token = {
    _createToken(userId, clock.instant().toEpochMilli, applicationSecret)
  }

  override def saveToken(userId: UserId, token: Token): ConnectionIO[Nothing, Unit] = ConnectionIO.right { conn =>
    val query =
      """insert into tokens(token, user_id, expires_at) values (?, ?, ?);"""
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    pstmt.setString(2, userId.value)
    pstmt.setTimestamp(3, Timestamp.from(clock.instant().plusSeconds(60 * 60 * 24)))
    pstmt.executeUpdate()
  }

  override def deleteToken(token: Token): ConnectionIO[Nothing, Boolean] = ConnectionIO.right { conn =>
    val query = "delete from tokens where token = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    val rows: Int = pstmt.executeUpdate()
    if (rows > 1) throw new RuntimeException
    if (rows == 1) true else false
  }

  override def fetchUserIdByToken(token: Token): ConnectionIO[Nothing, Option[UserId]] = ConnectionIO.right { conn =>
    val query = "select user_id from tokens where token = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    val mapping: ResultSet => UserId = row => UserId(UUID.fromString(row.getString("user_id")))
    select(pstmt, mapping)(conn)
  }

}

object TokenRepositoryImpl {

  private val ts: Array[Char] = (('A' to 'Z').toList :::
    ('a' to 'z').toList :::
    ('0' to '9').toList :::
    List('-', '.', '_', '~', '+', '/')).toArray

  def _createToken(seed: UserId, now: Long, salt: String): Token = {
    val rnd = new Random
    rnd.setSeed(now + seed.## + salt.##)

    val tsLen  = ts.length
    val length = 64

    Token(List.fill(length)(ts(rnd.nextInt(tsLen))).mkString)
  }

}
