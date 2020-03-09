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
import neko.chat.application.entity.{User, Auth, Email, RawPassword, HashedPassword}
import neko.chat.application.entity.User.{UserId, UserName}

class UserRepositoryImpl(
    dbPool: DBPool,
    clock: Clock,
    applicationSecret: String
) extends UserRepository {

  import UserRepositoryImpl._

  override def saveNewUser(
      userName: UserName,
      email: Email,
      hashedPassword: HashedPassword
  ): Either[Throwable, User] = {
    val user = User(UserId(UUID.randomUUID()), userName, clock.instant())
    val auth = Auth(email, hashedPassword, user.id)
    val io = for {
      _ <- insertUserIO(user)
      _ <- insertAuthIO(auth)
    } yield ()
    io.runTx(dbPool.getConnection()).map(_ => user)
  }

  override def createHashedPassword(rawPassword: RawPassword): HashedPassword = {
    _createHashedPassword(rawPassword, applicationSecret)
  }

  override def fetchUserIdBy(email: Email, hashedPassword: HashedPassword): Option[UserId] = {
    selectUserIdFromAuthsIO(email, hashedPassword)
      .runReadOnly(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
  }

  override def fetchBy(userId: UserId): Option[User] = {
    selectUserIO(userId)
      .runReadOnly(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
  }

  override def updateUserName(userId: UserId, newUserName: UserName): Unit = {
    updateUserNameIO(userId, newUserName)
      .runTx(dbPool.getConnection())
      .left
      .map { e: Throwable =>
        throw e
      }
      .merge
  }

}

object UserRepositoryImpl {

  val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

  def _createHashedPassword(rawPassword: RawPassword, applicationSecret: String): HashedPassword = {
    val keySpec = new PBEKeySpec(
      rawPassword.value.toCharArray,
      applicationSecret.getBytes,
      /* iterationCount = */ 10000,
      /* keyLength = */ 512 /* bytes */
    )
    val secretKey: SecretKey = secretKeyFactory.generateSecret(keySpec)
    val value                = Base64.getEncoder.encodeToString(secretKey.getEncoded)
    HashedPassword(value)
  }

  def selectUserIO(userId: UserId): ConnectionIO[Option[User]] = ConnectionIO { conn =>
    val query = """select * from users where id = ?;"""
    val mapping: ResultSet => User = row =>
      User(
        id = UserId(UUID.fromString(row.getString("id"))),
        name = UserName(row.getString("name")),
        createdAt = row.getTimestamp("created_at").toInstant
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, userId.asString)
    select(stmt, mapping)(conn)
  }

  def selectUserIdFromAuthsIO(email: Email, hashedPassword: HashedPassword): ConnectionIO[Option[UserId]] =
    ConnectionIO { conn =>
      val query                        = "select user_id from auths where email = ? and hashed_password = ?;"
      val mapping: ResultSet => UserId = row => UserId(UUID.fromString(row.getString("user_id")))
      val stmt                         = conn.prepareStatement(query)
      stmt.setString(1, email.value)
      stmt.setString(2, hashedPassword.value)
      select(stmt, mapping)(conn)
    }

  def insertUserIO(user: User): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into users(id, name, created_at) values (?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, user.id.asString)
    stmt.setString(2, user.name.value)
    stmt.setTimestamp(3, Timestamp.from(user.createdAt))
    stmt.executeUpdate()
    ()
  }

  def insertAuthIO(auth: Auth): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query = "insert into auths(email, hashed_password, user_id) values (?, ?, ?);"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, auth.email.value)
    pstmt.setString(2, auth.hashedPassword.value)
    pstmt.setString(3, auth.userId.asString)
    try {
      pstmt.executeUpdate()
    } catch {
      case e: SQLIntegrityConstraintViolationException =>
        throw new UserRepository.UserNotExistOrDuplicateUserNameException(e)
    }
    ()
  }

  def updateUserNameIO(userId: UserId, newUserName: UserName): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query = "update users set name = ? where id = ?;"
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, newUserName.value)
    pstmt.setString(2, userId.asString)
    val rows = pstmt.executeUpdate()
    if (rows != 1) throw new RuntimeException
    ()
  }

}
