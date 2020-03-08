package neko.chat.infra.db

import java.util.UUID
import java.time.Clock
import java.sql.{ResultSet, Timestamp}
import java.sql.SQLIntegrityConstraintViolationException

import javax.crypto.{SecretKey, SecretKeyFactory}
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

import neko.core.jdbc.{ConnectionIO, DBPool}
import neko.core.jdbc.query._

import neko.chat.application.repository.UserRepository
import neko.chat.application.entity.{User, Auth}

class UserRepositoryImpl(
  dbPool: DBPool,
  clock: Clock,
  applicationSecretSalt: String
) extends UserRepository {

  import UserRepositoryImpl._

  override def createNewUser(userName: UserName, email: Email, hashedPassword: HashedPassword): Either[Throwable, User] = {
    val user           = User(UUID.randomUUID(), userName, clock.instant())
    val auth           = Auth(email, hashedPassword, user.id)
    val io = for {
    _ <- insertUserIO(user)
    _ <- insertAuthIO(auth)
    } yield ()
    io.runTx(dbPool.getConnection())
    user
  }

  def createHashedPassword(rawPassword: RawPassword): HashedPassword = {
    val keySpec = new PBEKeySpec(
      rawPassword.value.toCharArray,
      applicationSecretSalt.getBytes,
      /* iterationCount = */ 10000,
      /* keyLength = */ 256 /* bytes */
    )
    val secretKey: SecretKey = secretKeyFactory.generateSecret(keySpec)
    val value = Base64.getEncoder.encodeToString(secretKey.getEncoded)
    HashedPassword(value)
  }

  override def fetchBy(userId: UUID): ConnectionIO[Option[User]] = ConnectionIO { conn =>
    val query = """select * from users where id = ?;"""
    val mapping: ResultSet => User = row =>
      User(
        id = UUID.fromString(row.getString("id")),
        screenName = row.getString("screen_name"),
        createdAt = row.getTimestamp("created_at").toInstant
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, userId.toString)
    select(stmt, mapping)(conn)
  }

}

object UserRepositoryImpl {

  def insertUserIO(user: User): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into users(id, screen_name, created_at) values (?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, user.id.toString)
    stmt.setString(2, user.screenName)
    stmt.setTimestamp(3, Timestamp.from(user.createdAt))
    stmt.executeUpdate()
    ()
  }

  override def insertAuthIO(auth: Auth): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query = "insert into auths(login_name, hashed_password, user_id) values (?, ?, ?);"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, auth.loginName)
    pstmt.setString(2, auth.hashedPassword)
    pstmt.setString(3, auth.userId.toString)
    try {
      pstmt.executeUpdate()
    } catch {
      case e: SQLIntegrityConstraintViolationException => throw new UserRepository.UserNotExistOrDuplicateUserNameException(e)
    }
    ()
  }

}
