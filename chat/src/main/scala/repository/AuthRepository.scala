package neko.chat.repository

import javax.crypto.{SecretKey, SecretKeyFactory}
import javax.crypto.spec.PBEKeySpec
import java.util.Base64
import neko.chat.entity.{User, Auth}
import neko.chat.auth.Token
import neko.core.jdbc.ConnectionIO

trait AuthRepository {

  def authenticate(token: Token): ConnectionIO[Option[User]]

  def login(loginName: String, rawPassword: String): ConnectionIO[Option[Token]]

  def logout(token: Token): ConnectionIO[Unit]

  def create(auth: Auth): ConnectionIO[Unit]

}

object AuthRepository {

  class LoginNameNotExistOrWrongPassword extends Exception

  private val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

  def generateHashedPassword(rawPassword: String, salt: Any): String = {
    val keySpec = new PBEKeySpec(
      rawPassword.toCharArray,
      salt.toString.getBytes,
      /* iterationCount = */ 10000,
      /* keyLength = */ 256 /* bytes */
    )
    val secretKey: SecretKey = secretKeyFactory.generateSecret(keySpec)
    Base64.getEncoder.encodeToString(secretKey.getEncoded)
  }

  class UserNotExistOrDuplicateUserNameException(e: Throwable) extends Exception(e)

}
