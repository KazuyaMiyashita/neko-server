package neko.chat.repository

import java.util.UUID
import java.time.Clock
import java.sql.{ResultSet, Timestamp}

import neko.chat.entity.User
import neko.chat.auth.Token
import neko.chat.entity.Auth
import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._

import java.sql.SQLIntegrityConstraintViolationException

class AuthRepositoryImpl(
    clock: Clock
) extends AuthRepository {

  import AuthRepository._

  override def authenticate(token: Token): ConnectionIO[Option[User]] = ConnectionIO { conn =>
    val query =
      """select * from tokens as t
        |  inner join users as u
        |    where t.user_id = u.id
        |  where token = ?;
        |""".stripMargin
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    val mapping: ResultSet => User = row =>
      User(
        id = UUID.fromString(row.getString("u.id")),
        screenName = row.getString("u.screen_name"),
        createdAt = row.getTimestamp("u.created_at").toInstant
      )
    select(pstmt, mapping)(conn)
  }

  override def login(loginName: String, rawPassword: String): ConnectionIO[Option[Token]] = ConnectionIO { conn =>
    val selectAuthQuery =
      "select * from auths where login_name = ?;"
    val selectAuthPstmt = conn.prepareStatement(selectAuthQuery)
    selectAuthPstmt.setString(1, loginName)
    val mapping: ResultSet => Auth = row =>
      Auth(
        loginName = row.getString("login_name"),
        hashedPassword = row.getString("hashed_password"),
        userId = UUID.fromString(row.getString("expires_at"))
      )
    val authOpt: Option[Auth] = select(selectAuthPstmt, mapping)(conn)

    authOpt.flatMap { auth =>
      if (auth.hashedPassword != generateHashedPassword(rawPassword, loginName)) None
      else {
        val token = Token.createToken(loginName)
        val insertTokenQuery =
          """insert into tokens(token, user_id, created_at) values (?, ?, ?);"""
        val insertTokenPstmt = conn.prepareStatement(insertTokenQuery)
        insertTokenPstmt.setString(1, token.value)
        insertTokenPstmt.setString(2, auth.userId.toString)
        insertTokenPstmt.setTimestamp(3, Timestamp.from(clock.instant().plusSeconds(60 * 60 * 24)))
        insertTokenPstmt.executeUpdate()
        Some(token)
      }
    }
  }

  override def logout(token: Token): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query = "delete from tokens where token = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, token.value)
    pstmt.executeUpdate()
    ()
  }

  override def create(auth: Auth): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query = "insert into auths(login_name, hashed_password, user_id) values (?, ?, ?);"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, auth.loginName)
    pstmt.setString(2, auth.hashedPassword)
    pstmt.setString(3, auth.userId.toString)
    try {
      pstmt.executeUpdate()
    } catch {
      case e: SQLIntegrityConstraintViolationException => throw new UserNotExistOrDuplicateUserNameException(e)
    }
    ()
  }

}
