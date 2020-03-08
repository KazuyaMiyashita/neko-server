package neko.chat.infra.db

import java.util.UUID
import java.time.{Clock, Instant}
import java.sql.{ResultSet, Timestamp}

import scala.util.Random

import neko.core.jdbc.{ConnectionIO, DBPool}
import neko.core.jdbc.query._

import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId
import neko.chat.application.repository.TokenRepository

class TokenRepositoryImpl(
    dbPool: DBPool,
    clock: Clock,
    applicationSecret: String
) extends TokenRepository {

  import TokenRepositoryImpl._

  override def createToken(userId: UserId): Token = {
    _createToken(userId, clock.instant().toEpochMilli, applicationSecret)
  }

  override def saveToken(userId: UserId, token: Token): Unit = {
    insertTokenIO(userId, token, clock.instant())
      .runTx(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
  }

  override def deleteToken(token: Token): Boolean = {
    deleteTokenIO(token)
      .runTx(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
  }

  override def fetchUserIdByToken(token: Token): Option[UserId] = {
    fetchUserIdByTokenIO(token)
      .runReadOnly(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
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

  def insertTokenIO(userId: UserId, token: Token, now: Instant): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into tokens(token, user_id, expires_at) values (?, ?, ?);"""
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    pstmt.setString(2, userId.asString)
    pstmt.setTimestamp(3, Timestamp.from(now.plusSeconds(60 * 60 * 24)))
    pstmt.executeUpdate()
  }

  def deleteTokenIO(token: Token): ConnectionIO[Boolean] = ConnectionIO { conn =>
    val query = "delete from tokens where token = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    val rows: Int = pstmt.executeUpdate()
    if (rows > 1) throw new RuntimeException
    if (rows == 1) true else false
  }

  def fetchUserIdByTokenIO(token: Token): ConnectionIO[Option[UserId]] = ConnectionIO { conn =>
    val query = "select * from tokens where token = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    val mapping: ResultSet => UserId = row => UserId(UUID.fromString(row.getString("token")))
    select(pstmt, mapping)(conn)
  }

}
